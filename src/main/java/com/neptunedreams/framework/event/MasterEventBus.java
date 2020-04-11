package com.neptunedreams.framework.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * This class serves as a facade for the event bus. The event bus instance is private, and all methods to post an event
 * are static methods in this class. This makes it easier to keep track of the events.
 * <p>
 * Under no circumstances should the EventBus instance be made available to other classes. By requiring all posts
 * to be done by static methods, we make it possible to use multiple EventBuses in a project, each with its own 
 * set of post methods. This design guarantee that a message can't get posted to the wrong EventBus.
 * The only mistake that can get made is registering a class with the wrong event bus. Consequently, 
 * each facade class should also have a unique name for its register method.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class MasterEventBus {
  private MasterEventBus() {
    master.register(this); // Register Dead Events.
  }

  @SuppressWarnings("HardCodedStringLiteral")
  private static EventBus master = new EventBus("master");

  /**
   * Register an instance with the master event bus
   * @param eventHandlerInstance The instance to register
   */
  public static void registerMasterEventHandler(Object eventHandlerInstance) {
    master.register(eventHandlerInstance);
  }

  // Data-Free Events:
  private static final LoadUIEvent uiEvent = new LoadUIEvent();
  private static final SearchNowEvent searchNowEvent = new SearchNowEvent();
  private static final UserRequestedNewRecordEvent userRequestedNewRecordEvent = new UserRequestedNewRecordEvent();

  // Simple public Event Classes (Classes that have no data)

  /**
   * A new record has been chosen and now needs to be loaded.
   */
  public static final class LoadUIEvent { }

  /**
   * The user interface has created a new, blank record.
   */
  public static final class UserRequestedNewRecordEvent { }

  /**
   * The user interface has changed the search requirements and a new search should be initiated.
   */
  public static final class SearchNowEvent { }

  // Public post methods

  /**
   * post a loadUserData message
   */
  public static void postLoadUserData() {
    master.post(uiEvent);
  }

  /**
   * post a userRequestedNewRecordEvent message
   */
  public static void postUserRequestedNewRecordEvent() {
    master.post(userRequestedNewRecordEvent);
  }

  /**
   * post a searchNowEvent message
   */
  public static void postSearchNowEvent() {
    master.post(searchNowEvent);
  }

  /**
   * post a ChangeRecordEvent message
   * @param record The affected record
   * @param <R> The type of the record
   */
  public static <R> void postChangeRecordEvent(R record) {
    master.post(new ChangeRecord<>(record));
  }

  /**
   * Log an error, just in case this gets called.
   * @param deadEvent The dead event
   */
  @Subscribe
  public void showDeadEvent(DeadEvent deadEvent) {
    //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral
    System.err.printf("Dead Event: %s of class %s%n", deadEvent, deadEvent.getClass());
  }
}
