![](assets/ModernDisguise.png)

## Description

ModernDisguise is a free lightweight open-source high quality library to help you add a disguise system to your
minecraft plugin

## Features

- Change a player's name (Server & Client sided)
- Change a player's skin (Server & Client sided)
- Change a player's entity type (Client sided & other players only)

## Supported versions

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
- 1.19.x (1_19_R1, 1_19_R2)

## WARNING

On versions that support Mojang Chat-Reports this plugin will add a ChatColor#RESET to the end of every message
to disable Mojang's Chat-Report feature.

## Maven

This project is deployed on a self-hosted repository currently

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
    <version>1.3</version>
    <scope>compile</scope>
</dependency>
```

## Gradle

Add this repo to your repositories block

```groovy
repositories {
    maven {
        name = "gravemc-repo"
        url = "https://repo.gravemc.net/releases/"
    }
}
```

and now add dependency

```groovy
dependencies {
    implementation 'dev.iiahmed:ModernDisguise:1.3'
}
```

## Usage

Here's an example usage of the API (easiest):

```java
import dev.iiahmed.disguise.*;

public class ExampleClass implements Listener {

    private final DisguiseProvider provider = DisguiseManager.getProvider();

    public ExampleClass() {
        DisguiseManager.setPlugin(ExamplePlugin.getInstance());
        // this is optional (it registers a PlaceholderAPI expansion for you)
        // placeholders are: %nick_name%, %nick_realname%, %nick_is_nicked% (%nick_is_disguised%)
        DisguiseManager.registerExpantion();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                // the boolean is whether this is a fake nickname or not
                .setName("BillBobbyBob", false)
                // you could as well use Disguise.Builder#setSkin(textures, signature)
                // which is more recommended
                // it's recommended to run this async since #setSkin from an online API will block the mainthread
                .setSkin(SkinAPI.MOJANG_UUID, "example-uuid")
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
        DisguiseManager.setPlugin(ExamplePlugin.getInstance());
        // this is optional (it registers a PlaceholderAPI expansion for you)
        // placeholders are: %nick_name%, %nick_realname%, %nick_is_nicked% (%nick_is_disguised%)
        DisguiseManager.registerExpantion();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                // the boolean is whether this is a fake nickname or not
                .setName("BillBobbyBob", false)
                // you could as well use Disguise.Builder#setSkin(textures, signature)
                // which is more recommended
                // it's recommended to run this async since #setSkin from API could block the mainthread
                .setSkin(SkinAPI.MOJANG_UUID, "example-uuid")
                // this will change the player into a zombie for others only
                .setEntityType(EntityType.ZOMBIE)
                .build();
        DisguiseResponse response = provider.disguise(player, disguise);
        // there are 7 responses other than DisguiseResponse#SUCCESS
        switch (response) {
            case SUCCESS -> player.sendMessage("Disguise is successful.");
            case FAIL_NAME_ALREADY_ONLINE -> player.sendMessage("There's already an online player with that nickname.");
            default -> player.sendMessage("Disguise is unsuccessful with the reason " + response.toString());
        }
    }

}
```

There's way more to it but I'd rather you figure it out on your own by checking the DisguiseProvider class :)

