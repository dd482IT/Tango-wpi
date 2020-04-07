package com.neptunedreams.framework.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import com.neptunedreams.framework.event.MasterEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:27 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecordModel<R> implements Serializable {
  private final transient List<RecordModelListener> listenerList = new LinkedList<>();

  // foundItems should be a RandomAccess list
  private List<@NonNull R> foundItems = new ArrayList<>();
  private int recordIndex = 0;
  private final Supplier<@NonNull R> constructor;
  private @NonNull Function<R, Integer> getIdFunction;
  private boolean isDirectionForward = true;

  public RecordModel(Supplier<@NonNull R> theConstructor, @NonNull Function<R, Integer> getIdFunction) {
    constructor = theConstructor;
    this.getIdFunction = getIdFunction; 
  }

  public int getRecordIndex() {
    return recordIndex;
  }

  public int getSize() { return foundItems.size(); }

  public void addModelListener(RecordModelListener listener) {
    listenerList.add(listener);
  }

  public void removeModelListener(RecordModelListener listener) {
    listenerList.remove(listener);
  }

  public void setNewList(Collection<? extends R> records) {
    int priorSelectionId = (foundItems.size() > recordIndex) ? getIdFunction.apply(foundItems.get(recordIndex)) : 0;
    foundItems = new ArrayList<>(records);
    // Not sure if the "if" is needed, or if we can just always set the record index to zero.
    if (recordIndex >= foundItems.size()) {
      setRecordIndex(0);
    }
    if (foundItems.isEmpty()) {
      final R record;
      record = createNewEmptyRecord();
      foundItems.add(record);
    } else {
      if (priorSelectionId != 0) {
        setRecordById(priorSelectionId); // sets recordIndex to same record, or 0 if not found
      }
    }
    fireModelListChanged();
  }

  public @NonNull R createNewEmptyRecord() {
    return constructor.get();
//    return Objects.requireNonNull(emptyRecord);
//    assert emptyRecord != null;
//    return emptyRecord;
  }

  public void goNext() {
    assert !foundItems.isEmpty();
    int size = foundItems.size();
    int nextRecord = recordIndex + 1;
    if (nextRecord >= size) {
      nextRecord = 0;
    }
    setRecordIndex(nextRecord);
  }

  public void goPrev() {
    assert !foundItems.isEmpty();
    int nextRecord = recordIndex - 1;
    if (nextRecord < 0) {
      nextRecord = foundItems.size() - 1;
    }
    setRecordIndex(nextRecord);
  }

  public void goFirst() {
    assert !foundItems.isEmpty();
    setRecordIndex(0);
  }

  public void goLast() {
    assert !foundItems.isEmpty();
    setRecordIndex(foundItems.size()-1);
  }

  private void setRecordIndex(final int i) {
    if (i != recordIndex) {
      int prior = recordIndex;
      recordIndex = i;
      fireIndexChanged(i, prior);
    }
  }

  private void fireIndexChanged(final int i, int prior) {
    for (RecordModelListener modelListener: listenerList) {
      modelListener.indexChanged(i, prior);
    }
    MasterEventBus.postDataModelChangedEvent();
  }

  public void append(@NonNull R insertedRecord) {
    final int newIndex = foundItems.size();
    foundItems.add(insertedRecord);
    setRecordIndex(newIndex);
    fireModelListChanged();
  }

  public @NonNull R getFoundRecord() {
    if (!foundItems.isEmpty()) {
      return foundItems.get(recordIndex);
//      assert foundRecord != null;
//      return Objects.requireNonNull(foundRecord);
    }
    R emptyRecord = createNewEmptyRecord();
    foundItems.add(emptyRecord);
    fireModelListChanged(); // Is it dangerous to fire the listener before returning the record?
    return emptyRecord;
  }

  /**
   * Sets the record index to point to the provided record, if it's in the found set. This is to preserve the current
   * record if it's in the found set. If it's not, leaves the record index unchanged.
   * @param recordId The ID of the record to set
   */
  private void setRecordById(int recordId) {
    int index = 0;
    for (R r : foundItems) {
      if (recordId == getIdFunction.apply(r)) {
        setRecordIndex(index);
        return;
      }
      index++;
    }
  }

  public @NonNull R getRecordAt(int index) {
    return foundItems.get(index);
  }

  /**
   * Delete the selected item, conditionally, from the model only. This doesn't delete anything from the database.
   * @param notify Fire appropriate listeners after deleting
   * @param index The index of the record to delete. This method does nothing if index is < 0,
   */
  @SuppressWarnings("BooleanParameter")
  public void deleteSelected(boolean notify, int index) {
    if (index >= 0) {
      foundItems.remove(index);
      if (foundItems.isEmpty()) {
        foundItems.add(createNewEmptyRecord());
      }
      if (recordIndex >= foundItems.size()) {
        recordIndex--; // Should we call setRecordIndex() here?
        assert recordIndex >= 0;
  //      if (recordIndex < 0) {
  //        recordIndex = 0;
  //      }
        if (notify) {
          fireIndexChanged(recordIndex, index);
        }
      }
      if (notify) {
        fireModelListChanged();
      }
    }
  }

  private void fireModelListChanged() {
    int size = foundItems.size();
    for (RecordModelListener listener : listenerList) {
      listener.modelListChanged(size);
    }
  }
}
