# ModernDisguise

## Description
ModernDisguise is a free open-source high quality library to help you add a disguise/nick system in your minecraft plugin

## Maven

This project is deployed on a self-hosted repository currently

here's the repo
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

Here's an example usage of the API (easiest)

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
                // you could as well use DisguiseBuilder#setSkin(textures, signature)
                .setSkin(SkinAPI.MINETOOLS_UUID, "example-uuid")
                .build();
        provider.disguise(player, disguise);
    }
    
}
```