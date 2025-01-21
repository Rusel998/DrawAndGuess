package ru.itis.drawandguess.shared;

public class Message {
    private String type;  // Тип сообщения (например, "DRAW", "GUESS")
    private Object data;// Данные сообщения
    public Message(String type, Object data) {
        this.type = type;
        this.data = data;

    }

    public Object getData() {
        return data;
    }


    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}
