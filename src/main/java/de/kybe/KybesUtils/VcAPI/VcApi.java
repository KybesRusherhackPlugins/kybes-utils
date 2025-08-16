package de.kybe.KybesUtils.VcAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import de.kybe.KybesUtils.VcAPI.model.ChatsResponse;
import de.kybe.KybesUtils.VcAPI.model.ConnectionsResponse;
import de.kybe.KybesUtils.VcAPI.model.DeathsResponse;
import de.kybe.KybesUtils.VcAPI.model.KillsResponse;
import org.rusherhack.core.logging.ILogger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;

public class VcApi {
    private final HttpClient httpClient;
    private final ILogger logger;
    private final Gson gson;
    private final String version;

    public VcApi(final ILogger logger, final String version) {
        this.logger = logger;
        this.version = version;
        this.httpClient = HttpClient.newBuilder()
                .build();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString()))
                .setStrictness(Strictness.LENIENT)
                .create();
    }

    public Optional<ChatsResponse> getChats(final String playerName, final int page) {
        String uri;
        if (playerName == null || playerName.isEmpty()) {
            uri = "https://api.2b2t.vc/chats?pageSize=100&page=" + page;
        } else {
            if (playerName.contains("-")) {
                uri = "https://api.2b2t.vc/chats?pageSize=100&uuid=" + playerName + "&page=" + page;
            } else {
                uri = "https://api.2b2t.vc/chats?pageSize=100&playerName=" + playerName + "&page=" + page;
            }
        }
        return get(uri, ChatsResponse.class);
    }

    public Optional<DeathsResponse> getDeaths(final String playerName, final int page) {
        String uri;
        if (playerName.contains("-")) {
            uri = "https://api.2b2t.vc/deaths?pageSize=100&uuid=" + playerName + "&page=" + page;
        } else {
            uri = "https://api.2b2t.vc/deaths?pageSize=100&playerName=" + playerName + "&page=" + page;
        }
        return get(uri, DeathsResponse.class);
    }

    public Optional<KillsResponse> getKills(final String playerName, final int page) {
        String uri;
        if (playerName.contains("-")) {
            uri = "https://api.2b2t.vc/kills?pageSize=100&uuid=" + playerName + "&page=" + page;
        } else {
            uri = "https://api.2b2t.vc/kills?pageSize=100&playerName=" + playerName + "&page=" + page;
        }
        return get(uri, KillsResponse.class);
    }

    public Optional<ConnectionsResponse> getConnections(final String playerName, final int page) {
        String uri;
        if (playerName.contains("-")) {
            uri = "https://api.2b2t.vc/connections?pageSize=100&uuid=" + playerName + "&page=" + page;
        } else {
            uri = "https://api.2b2t.vc/connections?pageSize=100&playerName=" + playerName + "&page=" + page;
        }
        return get(uri, ConnectionsResponse.class);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private <T> Optional<T> get(final String uri, final Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .setHeader("User-Agent", "rusherhack-kybe-utils/" + version)
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<InputStream> response = this.httpClient.send(request, ofInputStream());
            try (JsonReader reader = new JsonReader(new InputStreamReader(response.body()))) {
                return Optional.ofNullable(this.gson.fromJson(reader, responseType));
            }
        } catch (final Exception e) {
            logger.error("Failed querying " + uri);
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
