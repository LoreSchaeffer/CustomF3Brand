package it.multicoredev.cf3b;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * BSD 3-Clause License
 * <p>
 * Copyright (c) 2021 - 2024, Lorenzo Magni
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Config {
    private static final List<String> COMMENT = List.of(
            "Change the brand message of your server!",
            "You can set a static text (if the list contains a single string) or animated text (if the list contains more strings).",
            "To format the message use the MiniMessage format (https://docs.advntr.dev/minimessage/format.html).",
            "(You could also use legacy text color codes, but it's strongly discouraged).",
            "Warning! Hexadecimal colors are not supported due to Minecraft limitations.",
            "",
            "You can also set the update period of the brand message to make a more or less smooth animation.",
            "Lower values of the update period will make the animation faster. The default value is 100.",
            "Lower values of the update period means that a lot more packets will be sent to each player, so be careful with this value.",
            "",
            "This plugin also support some placeholders that will be automatically replaced by the plugin with the correct value:",
            "  {name} - replaced with the player's name",
            "  {displayname} - replaced with the player's display name (Velocity does not have displayname so it will fallback to name)",
            "  {server} - replaced with the name of the player (Bungeecord/Velocity only)",
            "  {spigot} - replaced with the spigot brand (Bungeecord/Velocity only)",
            "",
            "If you are using the plugin on a Spigot or fork server, you can also use PlaceholderAPI placeholders.",
            "",
            "Don't change the debug value unless you know what you are doing."
    );

    @SerializedName("f3_brand")
    public List<String> f3Brand;
    private static final List<String> f3BrandDef = List.of(
            "<gold>MyServer</gold>",
            "<yellow>M</yellow><gold>yServer</gold>",
            "<white>M</white><yellow>y</yellow><gold>Server</gold>",
            "<yellow>M</yellow><white>y</white><yellow>S</yellow><gold>erver</gold>",
            "<gold>M<yellow>y<white>S<yellow>e<gold>rver",
            "<gold>My<yellow>S<white>e<yellow>r<gold>ver",
            "<gold>MyS<yellow>e<white>r<yellow>v<gold>er",
            "<gold>MySe<yellow>r<white>v<yellow>e<gold>r",
            "<gold>MySer<yellow>v<white>e<yellow>r",
            "<gold>MyServ<yellow>e<white>r",
            "<gold>MyServe<yellow>r",
            "<gold>MyServer",
            "<gold>MyServe<yellow>r",
            "<gold>MyServ<yellow>e<white>r",
            "<gold>MySer<yellow>v<white>e<yellow>r",
            "<gold>MySe<yellow>r<white>v<yellow>e<gold>r",
            "<gold>MyS<yellow>e<white>r<yellow>v<gold>er",
            "<gold>My<yellow>S<white>e<yellow>r<gold>ver",
            "<gold>M<yellow>y<white>S<yellow>e<gold>rver",
            "<yellow>M<white>y<yellow>S<gold>erver",
            "<white>M<yellow>y<gold>Server",
            "<yellow>M<gold>yServer"
    );

    @SerializedName("update_period")
    public Long updatePeriod;
    private static final Long updatePeriodDef = 100L;

    @SerializedName("debug")
    public Boolean debug;
    private static final boolean debugDef = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private transient File file;

    private Config() {
    }

    public Config(File file) {
        this.file = file;
    }

    public static Config load(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder json = new StringBuilder();
            String line;

            boolean multiLineComment = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("/*")) {
                    multiLineComment = true;

                    line = line.substring(0, line.indexOf("/*"));
                    if (!line.isBlank()) json.append(line);
                }

                if (!multiLineComment && line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                    if (!line.isBlank()) json.append(line);
                }

                if (multiLineComment) {
                    if (line.contains("*/")) {
                        multiLineComment = false;

                        line = line.substring(line.indexOf("*/") + 2);
                        if (!line.isBlank()) json.append(line);
                    } else {
                        continue;
                    }
                }

                if (!line.isBlank()) json.append(line);
            }

            Config config = GSON.fromJson(json.toString(), Config.class);
            config.file = file;

            return config;
        }
    }

    public boolean init() {
        boolean changed = false;

        if (f3Brand == null) {
            f3Brand = f3BrandDef;
            changed = true;
        }

        if (updatePeriod == null) {
            updatePeriod = updatePeriodDef;
            changed = true;
        }

        if (debug == null) {
            debug = debugDef;
            changed = true;
        }

        return changed;
    }

    public void save() throws IOException {
        String json = GSON.toJson(this);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("/*\n");
            for (String line : COMMENT) writer.write(" * " + line + "\n");
            writer.write(" */\n\n");
            writer.write(json);
        }
    }
}
