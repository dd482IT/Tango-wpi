package com.neptunedreams.framework.ui;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.RecordSelectionModel;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.util.StringStuff;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Handles user input and output by sending commands to the data model.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 11:27 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
public final class RecordController<R, PK, F extends DBField> implements RecordModelListener {
  private static final Integer ZERO = 0;
  // For DerbyRecordDao, E was Record.FIELD
//  private E order = Record.FIELD.SOURCE;
  private F order;
  private final Dao<R, PK, F> dao;
  private final RecordSelectionModel<? extends R> recordSelectionModel;
  @NotOnlyInitialized
  private final RecordModel<R> model;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  private RecordController(
      Dao<R, PK, F> theDao,
      RecordSelectionModel<? extends R> recordSelectionModel,
      F initialOrder,
      Supplier<@NonNull R> recordConstructor,
      Function<R, Integer> getIdFunction
  ) {
    dao = theDao;
    this.recordSelectionModel = recordSelectionModel;
    model = new RecordModel<>(recordConstructor, getIdFunction);
    order = initialOrder;
    AutoSave.engage(this::saveCurrentRecord); // warning suppressed here.
  }

  /**
   * Construct a RecordController
   * @param theDao The DAO
   * @param recordSelectionModel The selection model from which the controller gets the selected record
   * @param initialOrder The initial order of the records
   * @param recordConstructor Constructs a new, blank record
   * @param getIdFunction Function to  get the ID from the record
   * @param <RR> The record type
   * @param <PPK> The primary key type
   * @param <FF> type of the initial and subsequent record orders
   * @return A constructed and initialized RecordController
   */
  public static <RR, PPK, FF extends DBField> RecordController<RR, PPK, FF> createRecordController(
      Dao<RR, PPK, FF> theDao,
      RecordSelectionModel<? extends RR> recordSelectionModel,
      FF initialOrder,
      Supplier<@NonNull RR> recordConstructor,
      Function<RR, Integer> getIdFunction
  ) {
    RecordController<RR, PPK, FF> recordController = new RecordController<>(theDao, recordSelectionModel, initialOrder, recordConstructor, getIdFunction);
    recordController.model.addModelListener(recordController);
    return recordController;
  }

  public RecordModel<R> getModel() {
    return model;
  }
  
  public Dao<R, PK, F> getDao() { return dao; }

  /**
   * Specify the order, chosen by the user, of the returned records, 
   * @param theOrder The field by which the results will be ordered
   */
  public void specifyOrder(F theOrder) {
    order = theOrder;
  }

  public F getOrder() {
    return order;
  }

  private void loadNewRecord(@NonNull R record) {
    saveCurrentRecord();
    MasterEventBus.postChangeRecordEvent(record);
  }

  void saveCurrentRecord() {
    R currentRecord = recordSelectionModel.getCurrentRecord(); // Move this back to where the comment is

    if (recordSelectionModel.isRecordDataModified()) {
      try {
        MasterEventBus.postLoadUserData();
        dao.insertOrUpdate(currentRecord);
      } catch (SQLException e) {
        ErrorReport.reportException("Insert", e);
      }
    }
  }

  /**
   * And a new, blank record to the end of the model.
   */
  public void addBlankRecord() {
    // If the last record is already blank, just go to it
    final int lastIndex = model.getSize() - 1;
    @NonNull R lastRecord = model.getRecordAt(lastIndex);
    assert lastRecord != null;
    final PK lastRecordKey = dao.getPrimaryKey(lastRecord);
    
    // If we are already showing an unchanged blank record...
    if ((model.getRecordIndex() == lastIndex) 
        && ((lastRecordKey == null) || (lastRecordKey.equals(ZERO))) 
        && !recordSelectionModel.isRecordDataModified()) {
      // ... we don't bother to create a new one.
      loadNewRecord(lastRecord);
    } else {
      R emptyRecord = model.createNewEmptyRecord();
      model.append(emptyRecord);
      loadNewRecord(emptyRecord);
    }
  }

  /**
   * Copies the current record into a new blank record. Fields to copy are determined by the BiConsumer {@code copyMethodFromTo}
   * @param fieldsToCopy A list of FieldBindings contianing the field values that need to be copied to the new record. 
   */
  public void copyCurrentRecord(Collection<FieldBinding.EditableFieldBinding<R, ?, ?>> fieldsToCopy) {
    int currentIndex = model.getRecordIndex();
    addBlankRecord();
    R original = model.getRecordAt(currentIndex);
    R newModel = model.getFoundRecord();
    for (FieldBinding.EditableFieldBinding<R, ?, ?> binding: fieldsToCopy) {
      copyValue(original, newModel, binding);
    }
  }
  
  private <V> void copyValue(R original, R newModel, FieldBinding.EditableFieldBinding<R, V, ?> binding) {
    V value = binding.getValue(original);
    binding.setValue(newModel, value);
    binding.prepareEditor(newModel);
  }

  /**
   * This executes on the event thread. It gets called when a search is done and new records are set.
   * @param theFoundItems
   */
  public void setFoundRecords(final Collection<@NonNull ? extends R> theFoundItems) {
    model.setNewList(theFoundItems);
    if (model.getSize() > 0) {
      final R selectedRecord = model.getFoundRecord();
      if (!selectedRecord.equals(recordSelectionModel.getCurrentRecord())) {
        loadNewRecord(selectedRecord);
      }
    }
  }

  /**
   * Finds the specified text in the specified field, and display them in the user interface.
   * @param dirtyText The text to find, uncleaned
   * @param field The field in which to search
   * @param searchOption The selected search option
   */
  public void findTextInField(String dirtyText, final F field, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<@NonNull R> foundItems = findRecordsInField(text, field, searchOption);
      setFoundRecords(foundItems);
    } catch (SQLException e) {
      ErrorReport.reportException(String.format("Find Text in Field %s with %s", field, searchOption), e);
    }
  }

  Collection<@NonNull R> findRecordsInField(final String text, final F field, SearchOption searchOption) throws SQLException {
    // If the user has changed the current record, we need to save those changes before searching, because The find
    // will retrieve values from the database, not from what's on-screen.
    loadNewRecord(model.getFoundRecord());

    if (text.trim().isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findWhole:
          return dao.findInField(text, field, getOrder());
        case findAll:
          return dao.findAllInField(field, getOrder(), StringStuff.splitText(text));
        case findAny:
          return dao.findAnyInField(field, getOrder(), StringStuff.splitText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));
      }
    }
  }

  /**
   * Find text in any field of the database.
   * @param dirtyText The text to find, without cleaning or wildcards
   * @param searchOption The search option (Find all, find any, etc)
   */
  public void findTextAnywhere(String dirtyText, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<@NonNull R> foundItems = findRecordsAnywhere(text, searchOption);
      setFoundRecords(foundItems);
    } catch (SQLException e) {
      ErrorReport.reportException("Find Text anywhere", e);
    }
  }
  
  Collection<@NonNull R> findRecordsAnywhere(final String text, SearchOption searchOption) throws SQLException {
    // If the user has changed the current record, we need to save those changes before searching, because The find
    // will retrieve values from the database, not from what's on-screen.
    loadNewRecord(model.getFoundRecord());

    if (text.isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findWhole:
          return dao.find(text, getOrder());
        case findAll:
          return dao.findAll(getOrder(), StringStuff.splitText(text));
        case findAny:
          return dao.findAny(getOrder(), StringStuff.splitText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));
      }
    }
  }

  @Override
  public void modelListChanged(final int newSize) {
    
  }

  /**
   * Finds records holding the search text, in the specified field, or anywhere, depending on the value of searchField.
   * @param searchField The field in which to search, which could be all fields
   * @param searchOption The user-selected search option
   * @param searchText The text to search for
   * @return A collection of the found records.
   */
  public Collection<@NonNull R> retrieveNow(final F searchField, final SearchOption searchOption, final String searchText) {
    try {
      if (searchField.isField()) {
        return findRecordsInField(searchText, searchField, searchOption);
      } else {
        return findRecordsAnywhere(searchText, searchOption);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return new LinkedList<>();
    }
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadNewRecord(model.getFoundRecord());
  }

  /**
   * Delete the specified record
   * @param selectedRecord The record to delete
   * @throws SQLException Most likely if the record is not found.
   */
  public void delete(final R selectedRecord) throws SQLException {
    dao.delete(selectedRecord);
  }
}