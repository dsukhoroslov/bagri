package com.bagri.tools.vvm.event;


public class ApplicationEvent extends Event {
    public static final String DISPATCH_WINDOW_CLOSING_ACTION = "bagri.event.dispatch:WINDOW_CLOSING";
    private String command;

    public ApplicationEvent(Object source, String command) {
        super(source);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
