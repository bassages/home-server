package nl.wiegman.home.model.event;

import org.springframework.context.ApplicationEvent;

public class UpdateEvent extends ApplicationEvent {

    public Object updatedObject;

    public UpdateEvent(Object updatedObject) {
        super(UpdateEvent.class.getSimpleName());
        this.updatedObject = updatedObject;
    }

    public Object getUpdatedObject() {
        return updatedObject;
    }
}
