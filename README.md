![](assets/ModernDisguise.png)

## Description
ModernDisguise is a free open-source high quality library to help you add a disguise/nick system in your minecraft plugin

## Supported versions
here's a list of supported versions so far:

- 1.8.8 (1_8_R3)
- 1.9.4 (1_9_R2)
- 1.10.x (1_10_R1)
- 1.11.x (1_11_R1)
- 1.12.x (1_12_R1)
- 1.13.2 (1_13_R2)
- 1.14.x (1_14_R1)
- 1.15.x (1_15_R1)
- 1.16.5 (1_16_R3)
- 1.17.x (<== NO MORE NMS)

## Maven

This project is deployed on a self-hosted repository currently

here's the repo

**TEMPORARILY OFFLINE**
```xml
<repositories>
    <repository>
        <id>iiahmed-dev</id>
        <url>https://repo.iiahmed.dev/repositories/public/</url>
    </repository>
</repositories>
```
and here's the dependancy
```xml
<dependency>
    <groupId>dev.iiahmed</groupId>
    <artifactId>ModernDisguise</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

## Usage

Here's an example usage of the API (easiest):

```java
import dev.iiahmed.disguise.*;

public class ExampleClass implements Listener {
    
    private final DisguiseProvider provider = DisguiseManager.getProvider();
    
    public ExampleClass() {
        provider.setPlugin(ExamplePlugin.getInstance());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                // the boolean is whether this is a fkae nickname or not
                .setName("BillBobbyBob", false)
                // you could as well use Disguise.Builder#setSkin(textures, signature)
                // which is more recommended
                // it's recommended to run this async since #setSkin from API could block the mainthread
                .setSkin(SkinAPI.MINETOOLS_UUID, "example-uuid")
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
        provider.setPlugin(ExamplePlugin.getInstance());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Disguise disguise = Disguise.builder()
                // the boolean is whether this is a fkae nickname or not
                .setName("BillBobbyBob", false)
                // you could as well use Disguise.Builder#setSkin(textures, signature)
                // which is more recommended
                // it's recommended to run this async since #setSkin from API could block the mainthread
                .setSkin(SkinAPI.MINETOOLS_UUID, "example-uuid")
                .build();
        DisguiseResponse response = provider.disguise(player, disguise);
        switch (response) {
            case SUCCESS:
                player.sendMessage("Disguise is successful.");
                break;
            case FAIL_NAME_ALREADY_ONLINE:
                player.sendMessage("There's already an online player with that nickname.");
                break;
            default:
                player.sendMessage("Disguise is unsuccessful with the reason " + response.toString());
                break;
        }
    }
    
}
```