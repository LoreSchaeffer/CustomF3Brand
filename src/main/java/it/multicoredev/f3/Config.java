package it.multicoredev.f3;

import com.google.gson.annotations.SerializedName;
import it.multicoredev.mclib.json.JsonConfig;

import java.util.List;

/**
 * BSD 3-Clause License
 * <p>
 * Copyright (c) 2023, Lorenzo Magni
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
public class Config extends JsonConfig {
    private String _comment1;
    private String _comment2;
    private String _comment3;
    private String _comment4;
    private String _comment5;
    private String _comment6;
    private String _comment7;
    private String _comment8;
    private String _comment9;

    @SerializedName("f3_brand")
    public List<String> f3Brand;
    @SerializedName("update_period")
    public Long updatePeriod;


    @Override
    public Config init() {
        _comment1 = "You can set your brand message. This can be an array of made of a single string to make it static or can be a list of";
        _comment2 = "strings to create an animation or make it change with a specified period.";
        _comment3 = "These strings can contain Chat color codes (Hexadecimal values are not supported) and can contain tags that will be replaced";
        _comment4 = "automatically by the plugin; these tags are:";
        _comment5 = "{name} - replaced with the player's name";
        _comment6 = "{displayname} - replaced with te player's display name";
        _comment7 = "{server} - replaced with the name of player's the server (ONLY BUNGEECORD)";
        _comment8 = "{spigot} - replaced with the spigot one (ONLY BUNGEECORD)";
        _comment9 = "Any placeholder by PlaceholderAPI (ONLY SPIGOT)";

        if (f3Brand == null) f3Brand = List.of(
                "&6MyServer",
                "&eM&6yServer",
                "&fM&ey&6Server",
                "&eM&fy&eS&6erver",
                "&6M&ey&fS&ee&6rver",
                "&6My&eS&fe&er&6ver",
                "&6MyS&ee&fr&ev&6er",
                "&6MySe&er&fv&ee&6r",
                "&6MySer&ev&fe&er",
                "&6MyServ&ee&fr",
                "&6MyServe&er",
                "&6MyServer",
                "&6MyServe&er",
                "&6MyServ&ee&fr",
                "&6MySer&ev&fe&er",
                "&6MySe&er&fv&ee&6r",
                "&6MyS&ee&fr&ev&6er",
                "&6My&eS&fe&er&6ver",
                "&6M&ey&fS&ee&6rver",
                "&eM&fy&eS&6erver",
                "&fM&ey&6Server",
                "&eM&6yServer"
        );

        if (updatePeriod == null || updatePeriod < 1) updatePeriod = 50L;
        return this;
    }
}
