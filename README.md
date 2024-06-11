# Custom F3 Brand

### Edit your server name in debug screen!
#### It's simple, it's powerful, it's fully customizable!
Set your custom message or animation in the debug screen (F3) of your server. You just need to edit the config of the plugin, 
it's easy and intuitive.

## Features
- [x] Spigot version
    - PlaceholderAPI support
- [x] Bungeecord version
- [x] Velocity version
  - Velocity version is in alpha stage, please report any issue you find.

## Requirements
- Minecraft 1.16+
- Java 17+.
- ProtocolLib is required for the Spigot version.

## Installation
Download the plugin from [here](https://modrinth.com/plugin/customf3brand) and put it in your plugins folder.
If you are installing this plugin in a Spigot server download also [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/);
That's it!

## Commands
- ```/f3r``` to reload the config (spigot)
- ```/bf3r``` to reload the config (bungeecord)
- ```/vf3r``` to reload the config (velocity)

## Permissions
- ```cf3.reload``` to reload the config

## Warnings
- Do not reload the plugin in a velocity server! If you want to reload the config use the command ```/vf3r```.
- The configuration file is a JSON file with json5 extension. This is done to allow comments in the file but is parsed as a json. 
When editing the file make sure to follow the json syntax.

## Known issues
- Some plugins and mods to communicate needs certain brand names and may not work properly with this plugin.

## Contributing
To contribute to this repository just fork this repository make your changes or add your code and make a pull request.

## License
CustomF3Brand is released under "The 3-Clause BSD License". You can find a copy [here](https://github.com/MultiCoreNetwork/CustomF3Brand/blob/master/LICENSE)