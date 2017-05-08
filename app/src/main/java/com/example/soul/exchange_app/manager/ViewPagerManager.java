package com.example.soul.exchange_app.manager;

/**
 * Created by soul on 2017. 5. 8..
 */

public class ViewPagerManager {

    private static EventListener eventListener;

    public interface EventListener{
        void onReceivedEvent(int position);
    }

    public void setOnEventListener(EventListener eventListener){
        this.eventListener = eventListener;
    }

    public EventListener getInterface(){
        return eventListener;
    }
}
