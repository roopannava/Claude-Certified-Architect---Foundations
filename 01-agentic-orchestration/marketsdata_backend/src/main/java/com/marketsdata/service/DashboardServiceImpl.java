package com.marketsdata.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsdata.model.DashboardResponse;
import com.marketsdata.model.StockRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builds market dashboards by letting Claude decide which to show.
 *
 * Each dashboard (most shorted, day losers, top mutual funds) is exposed to the
 * model as a tool. The prompt drives the run: Claude reads it, calls the
 * dashboard tools the user asked for -- one, two, or all three -- and we
 * assemble the response from whatever it fetched. This is the manual tool-use
 * loop; we own every turn:
 *
 *   1. send the prompt + tool definitions;
 *   2. while the model asks to use tools, run them and hand back the results;
 *   3. stop when the model is done (no more tool_use blocks).
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /** Opus 5.5: most capable, strongest tool-use reasoning. */
    private static final String MODEL = "claude-opus-5-5";

    /** Safety valve so a misbehaving loop can never run forever. */
    private static final int MAX_TURNS = 10;

    private static final String SYSTEM_PROMPT = """
            You are a market dashboard assistant. Three dashboards are available, each exposed
            as a tool: most_shorted (most heavily shorted stocks), day_losers (today's biggest
            decliners) and top_mutual_funds (top-performing mutual funds). Read the user's
            request and call only the dashboard tools they ask for -- if they ask for everything
            or "all dashboards", call all three. After the tools return, give a short plain-text
            summary of what you pulled.
            """;

    /** Thread-safe; serializes the rows we hand back as tool results. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AnthropicClient client;
    private final ServiceFactory factory;

    public DashboardServiceImpl(AnthropicClient client, ServiceFactory factory) {
        this.client = client;
        this.factory = factory;
    }

    @Override
    public DashboardResponse createDashboard(String prompt) {
        // Each dashboard becomes one tool. The tools take no input -- calling one
        // just means "show this dashboard", so the schema is an empty object.
        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(16000L)
                .system(SYSTEM_PROMPT)
                .addTool(tool(ServiceType.MOST_SHORTED, "the most heavily shorted stocks"))
                .addTool(tool(ServiceType.DAY_LOSERS, "today's biggest losing stocks"))
                .addTool(tool(ServiceType.TOP_MUTUAL_FUNDS, "the top-performing mutual funds"))
                .addUserMessage(prompt);

        // Remember what each dashboard returned so we can build the response.
        Map<ServiceType, List<StockRow>> fetched = new EnumMap<>(ServiceType.class);

        Message message = client.messages().create(params.build());

        for (int turn = 0; turn < MAX_TURNS; turn++) {
            // Append the model's turn verbatim (including any thinking blocks) so
            // the conversation stays append-only across the loop.
            params.addMessage(message);

            List<ContentBlockParam> toolResults = new ArrayList<>();
            for (ContentBlock block : message.content()) {
                block.toolUse().ifPresent(toolUse -> {
                    ServiceType type = ServiceType.valueOf(toolUse.name().toUpperCase());
                    List<StockRow> data = factory.get(type).create();
                    fetched.put(type, data);
                    toolResults.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                            .toolUseId(toolUse.id())
                            .content(toJson(data))
                            .build()));
                });
            }

            // No tool calls this turn means the model is finished.
            if (toolResults.isEmpty()) {
                break;
            }

            // Hand every tool_result back in a single user message, then continue.
            params.addMessage(MessageParam.builder()
                    .role(MessageParam.Role.USER)
                    .contentOfBlockParams(toolResults)
                    .build());
            message = client.messages().create(params.build());
        }

        return new DashboardResponse(
                prompt,
                fetched.getOrDefault(ServiceType.MOST_SHORTED, List.of()),
                fetched.getOrDefault(ServiceType.DAY_LOSERS, List.of()),
                fetched.getOrDefault(ServiceType.TOP_MUTUAL_FUNDS, List.of()));
    }

    private Tool tool(ServiceType type, String describes) {
        return Tool.builder()
                .name(type.name().toLowerCase())
                .description("Show " + describes + ".")
                .inputSchema(Tool.InputSchema.builder().build())
                .build();
    }

    private String toJson(List<StockRow> rows) {
        try {
            return OBJECT_MAPPER.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize dashboard rows for the tool result", e);
        }
    }
}
