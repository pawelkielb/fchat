package pl.pawelkielb.fchat;

import pl.pawelkielb.fchat.data.Message;
import pl.pawelkielb.fchat.data.Name;
import pl.pawelkielb.fchat.packets.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

public class PacketEncoder {
    public byte[] toBytes(Packet packet) {
        byte[] packetBytes;
        if (packet instanceof SendMessagePacket sendMessagePacket) {
            packetBytes = toBytes(sendMessagePacket);
        } else if (packet instanceof RequestMessagesPacket requestMessagesPacket) {
            packetBytes = toBytes(requestMessagesPacket);
        } else if (packet instanceof UpdateChannelPacket updateChannelPacket) {
            packetBytes = toBytes(updateChannelPacket);
        } else if (packet instanceof RequestLivePacket requestLivePacket) {
            packetBytes = toBytes(requestLivePacket);
        } else if (packet instanceof AcknowledgePacket acknowledgePacket) {
            packetBytes = toBytes(acknowledgePacket);
        } else if (packet instanceof LoginPacket loginPacket) {
            packetBytes = toBytes(loginPacket);
        } else if (packet instanceof ChannelUpdatedPacket channelUpdatedPacket) {
            packetBytes = toBytes(channelUpdatedPacket);
        } else {
            throw new IllegalArgumentException("This packet type is not supported");
        }

        return packetBytes;
    }

    private static String propertiesToString(Properties properties) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        try {
            properties.store(outputStreamWriter, null);
        } catch (IOException ignore) {
        }

        return outputStream.toString();
    }

    public byte[] toBytes(SendMessagePacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "SendMessage");
        properties.setProperty("author", packet.message().author().value());
        properties.setProperty("channel", packet.channel().toString());
        properties.setProperty("message", packet.message().content());
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(RequestMessagesPacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "RequestMessages");
        properties.setProperty("channel", packet.channel().toString());
        properties.setProperty("count", String.valueOf(packet.count()));
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(UpdateChannelPacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "UpdateChannel");
        properties.setProperty("channel", packet.channel().toString());
        properties.setProperty("name", packet.name().value());
        properties.setProperty("members", packet.members().stream().map(Name::value).collect(Collectors.joining(",")));
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(RequestLivePacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "RequestLive");
        properties.setProperty("channel", packet.channel().toString());
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(AcknowledgePacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "Acknowledge");
        properties.setProperty("packet_id", packet.packetId().toString());
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(LoginPacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "Login");
        properties.setProperty("username", packet.username().value());
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public byte[] toBytes(ChannelUpdatedPacket packet) {
        Properties properties = new Properties();
        properties.setProperty("type", "ChannelUpdated");
        properties.setProperty("packet_id", packet.packetID().toString());
        properties.setProperty("channel", packet.channel().toString());
        properties.setProperty("name", packet.name().value());
        String packetString = propertiesToString(properties);

        return packetString.getBytes();
    }

    public Packet decode(byte[] packetBytes) {
        String packetString = new String(packetBytes);
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(packetString));
        } catch (IOException e) {
            throw new AssertionError();
        }
        String packetType = properties.getProperty("type");
        if (packetType == null) {
            throw new PacketDecodeException("Unknown packet format");
        }

        return switch (packetType) {
            case "SendMessage" -> {
                Name author = Name.of(properties.getProperty("author"));
                UUID channel = UUID.fromString(properties.getProperty("channel"));
                String message = properties.getProperty("message");

                yield new SendMessagePacket(channel, new Message(author, message));
            }

            case "RequestMessages" -> {
                UUID channel = UUID.fromString(properties.getProperty("channel"));
                int count = Integer.parseInt(properties.getProperty("count"));

                yield new RequestMessagesPacket(channel, count);
            }

            case "UpdateChannel" -> {
                UUID channelId = UUID.fromString(properties.getProperty("channel"));
                Name channelName = Name.of(properties.getProperty("name"));
                List<Name> members = Arrays.stream(properties.getProperty("members").split(",")).map(Name::of).toList();

                yield new UpdateChannelPacket(channelId, channelName, members);
            }

            case "RequestLivePacket" -> {
                UUID channel = UUID.fromString(properties.getProperty("channel"));

                yield new RequestLivePacket(channel);
            }

            case "Acknowledge" -> {
                UUID packetId = UUID.fromString(properties.getProperty("packet_id"));

                yield new AcknowledgePacket(packetId);
            }

            case "Login" -> {
                Name username = Name.of(properties.getProperty("username"));

                yield new LoginPacket(username);
            }

            case "ChannelUpdated" -> {
                UUID packetId = UUID.fromString(properties.getProperty("packet_id"));
                UUID channel = UUID.fromString(properties.getProperty("channel"));
                Name name = Name.of(properties.getProperty("name"));

                yield new ChannelUpdatedPacket(packetId, channel, name);
            }

            default -> throw new PacketDecodeException("Unknown packet type");
        };
    }
}
