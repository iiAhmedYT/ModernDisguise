![](assets/ModernDisguise.png)

## 💬 Description
ModernDisguise is a free lightweight open-source high quality library to help you add a disguise system to your
minecraft plugin

Here's the [SpigotMC Forum](https://www.spigotmc.org/threads/moderndisguise-a-lightweight-free-open-source-disguise-library.582167/)

## 😎 Features
You can change the player's:
- Name (Server side)
- Skin (Server side)
- EntityType (up to 82 entities) (Client side & other players only can see it)

## ✅ Supported versions
- 1.8.8 (1_8_R3)
- 1.9.4 (1_9_R2)
- 1.10.x (1_10_R1)
- 1.11.x (1_11_R1)
- 1.12.x (1_12_R1)
- 1.13.x (1_13_R1, 1_13_R2)
- 1.14.x (1_14_R1)
- 1.15.x (1_15_R1)
- 1.16.x (1_16_R1, 1_16_R2, 1_16_R3)
- 1.17.x (1_17_R1)
- 1.18.x (1_18_R1, 1_18_R2)
- 1.19.x (1_19_R1, 1_19_R2, 1_19_R3)
- 1.20.x (1_20_R1, 1_20_R2, 1_20_R3, 1_20_R4)
- 1.21.x (1_21_R1, 1_21_R2, 1_21_R3)

## ⚠️ WARNING
On versions that support Mojang Chat-Reports this library disables that feature in order for disguised players to chat.
You can disable it by using DisguiseProvider#allowOverrideChat(false)

Example:
```java
public class ExampleClass {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public ExampleClass() {
        boolean allowEntities = getConfig().getBoolean("allow-entity-disguises");
        DisguiseManager.initialize(ExamplePlugin.getInstance(), allowEntities);
        provider.allowOverrideChat(false);
    }

}
```

## ➕ Add to your project
### Maven
Add this repo to your repositories:

```xml
<repository>
    <id>gravemc-repo</id>
    <url>https://repo.gravemc.net/releases/</url>
</repository>
```

and then add this dependancy:
```xml
<dependency>
    <groupId>dev.iiahmed</groupId>
    <artifactId>ModernDisguise</artifactId>
    <version>3.5</version>
    <scope>compile</scope>
</dependency>
```
and you can relocate them as well, here's an example maven-shade-plugin config:
```xml
<configuration>
    <filters>
        <filter>
            <artifact>*:*</artifact>
            <excludes>
                <exclude>META-INF/</exclude>
            </excludes>
        </filter>
    </filters>
    <relocations>
        <relocation>
            <pattern>dev.iiahmed.disguise</pattern>
            <shadedPattern>your.own.package.disguise</shadedPattern>
        </relocation>
    </relocations>
</configuration>
```
### Gradle
Add this repo to your repositories block:
```groovy
repositories {
    maven {
        name = "gravemc-repo"
        url = "https://repo.gravemc.net/releases/"
    }
}
```

and now add dependency:
```groovy
dependencies {
    implementation 'dev.iiahmed:ModernDisguise:3.5'
}
```

## 🧑‍💻 Usage
Here's an example usage of the API (easiest):

```java
import dev.iiahmed.disguise.*;

import java.util.UUID;
import java.util.regex.Pattern;

public class ExampleClass implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public ExampleClass() {
        boolean allowEntities = getConfig().getBoolean("allow-entity-disguises");
        DisguiseManager.initialize(ExamplePlugin.getInstance(), allowEntities);
        provider.allowOverrideChat(false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                .setName("BillBobbyBob")
                // you could as well use Disguise.Builder#setSkin(Skin)
                // or even Disguise.Builder#setSkin(uuid)
                // it's recommended to run this async since #setSkin from an online API will block the mainthread
                .setSkin(SkinAPI.MOJANG, UUID.fromString("d3db29ff-9bc2-4828-993f-3a75929280f5"))
                // this will change the player into a zombie for others only
                .setEntityType(EntityType.ZOMBIE)
                .build();
        provider.disguise(player, disguise);
    }

}
```

Here's an advanced way of using it:
```java
import dev.iiahmed.disguise.*;

public class ExampleClass implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public ExampleClass() {
        boolean allowEntities = getConfig().getBoolean("allow-entity-disguises");
        DisguiseManager.initialize(ExamplePlugin.getInstance(), allowEntities);
        provider.allowOverrideChat(false);
        provider.setNameLength(16);
        provider.setNamePattern(Pattern.compile("^[a-zA-Z0-9_]{1,16}$"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                .setName("BillBobbyBob")
                // you could as well use Disguise.Builder#setSkin(Skin)
                // or even Disguise.Builder#setSkin(uuid)
                // it's recommended to run this async since #setSkin from an online API will block the mainthread
                .setSkin(SkinAPI.MOJANG, UUID.fromString("d3db29ff-9bc2-4828-993f-3a75929280f5"))
                // this will change the player into a zombie for others only
                .setEntity(builder -> builder.setType(type).setAttribute(RangedAttribute.SCALE, 3.0D))
                .build();
        DisguiseResponse response = provider.disguise(player, disguise);
        // there are 8 responses other than DisguiseResponse#SUCCESS
        switch (response) {
            case SUCCESS -> player.sendMessage("Disguise is successful.");
            case FAIL_NAME_ALREADY_ONLINE -> player.sendMessage("There's already an online player with that name.");
            default -> player.sendMessage("Disguise is unsuccessful with the reason " + response.toString());
        }
    }

}
```

There's way more to it but I'd rather you figure it out on your own by checking the DisguiseProvider class :)

## 🏗️ Building
###### 1- Building Spigot Versions (could be skipped if downloaded using codemc)
All these versions have to be built using Spigot's BuildTools
- 1.8.8
- 1.9.4
- 1.10.2
- 1.11.2
- 1.12.2
- 1.13, 1.13.2
- 1.14.4
- 1.15.2
- 1.16.1, 1.16.3, 1.16.5

Versions from now-on should be built with the ```--remapped``` flag
- 1.17.1
- 1.18.1, 1.18.2
- 1.19.2, 1.19.3, 1.19.4
- 1.20.1, 1.20.2, 1.20.4, 1.20.6
- 1.21.1, 1.21.3, 1.21.4

###### 2- Cloning
You can either clone the repository using the famous ```git clone``` command or use your IDE's clone feature

Congratulations! Now you can build ModernDisguise with the command ```mvn clean install```

###### 3- Changing Version
Well, to be honest with maven its too much work to change the versions, however, not on my watch!
Just run ``mvn versions:set -DnewVersion=VERSION-HERE``, example: ``mvn versions:set -DnewVersion=3.0``

## 🪪 License
This project is licensed under the [GPL-3.0 License](LICENSE.md)
## ☀️ Credits
Shoutout to those people for helping me test this project and helping me find every single bug

- [Bermine](https://github.com/Bermiin)
- [Timury](https://github.com/MrKotex)
- [noobi](https://github.com/c0dingnoobi)

Thanks to [all the github contributors](https://github.com/iiAhmedYT/ModernDisguise/graphs/contributors)

Thanks JetBrains for providing me an Open-Source development tools License ❤️

![](assets/JetBrains.png)
