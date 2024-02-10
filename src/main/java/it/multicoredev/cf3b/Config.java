package it.multicoredev.cf3b;

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
        _comment3 = "To format the messages use MiniMessage (https://docs.advntr.dev/minimessage/format.html) (You can also use legacy text color codes, but it's not suggested). Warning! Hexadecimal coloros are not supported!";
        _comment4 = "There are some tags that will be automatically replaced by the plugin. These tags are:";
        _comment5 = "{name} - replaced with the player's name";
        _comment6 = "{displayname} - replaced with te player's display name";
        _comment7 = "{server} - replaced with the name of player's the server (ONLY BUNGEECORD/VELOCITY)";
        _comment8 = "{spigot} - replaced with the spigot brand (ONLY BUNGEECORD/VELOCITY)";
        _comment9 = "Any placeholder by PlaceholderAPI (ONLY SPIGOT)";

        if (f3Brand == null) f3Brand = List.of(
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

        if (updatePeriod == null || updatePeriod < 1) updatePeriod = 100L;
        return this;
    }
}
