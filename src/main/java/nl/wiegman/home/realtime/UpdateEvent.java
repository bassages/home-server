package nl.wiegman.home.realtime;

import org.springframework.context.ApplicationEvent;

public class UpdateEvent extends ApplicationEvent {

    public final Object updatedObject;

    public UpdateEvent(Object updatedObject) {
        super(UpdateEvent.class.getSimpleName());
        this.updatedObject = updatedObject;
    }

    public Object getUpdatedObject() {
        return updatedObject;
    }
}
