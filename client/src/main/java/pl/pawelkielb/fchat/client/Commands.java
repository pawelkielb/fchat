package pl.pawelkielb.fchat.client;

import pl.pawelkielb.fchat.client.config.ChannelConfig;
import pl.pawelkielb.fchat.client.config.ClientConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class Commands {

    public static String sanitizeAsPath(String string) {
        return string.replaceAll("[^a-zA-z ]", "");
    }

    public static void execute(String command,
                               List<String> args,
                               ClientConfig clientConfig,
                               ChannelConfig channelConfig,
                               Client client) {

        if (command.equals("init")) {
            if (clientConfig != null) {
                ExceptionHandler.onInitCalledInFchatDirectory();
            }

            try {
                client.init(Paths.get("."));
            } catch (FileWriteException e) {
                ExceptionHandler.onCannotWriteFile(e.getPath());
            }

            System.exit(0);
        } else {
            if (clientConfig == null) {
                ExceptionHandler.onClientPropertiesNotFound();
                return;
            }
        }

        // client properties cannot be null at this point

        switch (command) {
            case "create" -> {
                if (channelConfig != null) {
                    ExceptionHandler.onCommandUsedInChannelDirectory();
                }

                Path path = Paths.get(".");

                if (args.size() == 1) {
                    Name recipient = Name.of(args.get(0));
                    client.createPrivateChannel(path, recipient);
                } else {
                    var members = args
                            .subList(1, args.size())
                            .stream()
                            .map(Name::of)
                            .toList();

                    try {
                        client.createGroupChannel(path, Name.of(args.get(0)), members);
                    } catch (IOException e) {
                        ExceptionHandler.onNetworkException();
                    }
                }
            }

            case "send" -> {
                if (args.size() < 1) {
                    ExceptionHandler.onMessageNotProvided();
                    return;
                }

                if (channelConfig == null) {
                    ExceptionHandler.onCommandNotUsedInChannelDirectory();
                    return;
                }

                String message = String.join(" ", args.subList(1, args.size()));
                try {
                    client.sendMessage(channelConfig.id(), message);
                } catch (IOException e) {
                    ExceptionHandler.onNetworkException();
                }
            }

            case "read" -> {
                if (channelConfig == null) {
                    ExceptionHandler.onCommandNotUsedInChannelDirectory();
                }
            }

            case "sync" -> {

            }
        }
    }
}
