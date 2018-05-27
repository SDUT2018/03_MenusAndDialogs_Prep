package edu.rosehulman.fisherds.pointofsale;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "PointOfSale";
  private Item mCurrentItem;
  private TextView mNameTextView, mQuantityTextView, mDateTextView;
  private Item mClearedItem;
  private ArrayList<Item> mItems = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mNameTextView = findViewById(R.id.name_text);
    mQuantityTextView = findViewById(R.id.quantity_text);
    mDateTextView = findViewById(R.id.date_text);

    registerForContextMenu(mNameTextView);


    mCurrentItem = new Item("Roses", 12, new GregorianCalendar());
    mItems.add(mCurrentItem);
    mCurrentItem = new Item("Tulips", 100, new GregorianCalendar());
    mItems.add(mCurrentItem);
    showCurrentItem();


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        addEditItem(false);

        // This is temporary!
//        mCurrentItem = Item.getDefaultItem();
//        showCurrentItem();
      }
    });
  }

  private void addEditItem(final boolean isEdit) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    if (isEdit) {
      builder.setTitle("Edit this item");
    } else {
      builder.setTitle("Add a new item");
    }
    View view = getLayoutInflater().inflate(R.layout.dialog_add, null, false);
    builder.setView(view);
    final EditText nameEditText = view.findViewById(R.id.edit_name);
    final EditText quantityEditText = view.findViewById(R.id.edit_quantity);
    final CalendarView deliveryDateView = view.findViewById(R.id.calendar_view);
    final GregorianCalendar calendar = new GregorianCalendar();

    if (isEdit) {
      nameEditText.setText(mCurrentItem.getName());
      quantityEditText.setText("" + mCurrentItem.getQuantity());
      deliveryDateView.setDate(mCurrentItem.getDeliveryDateTime());
    }

    deliveryDateView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
      @Override
      public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        calendar.set(year, month, dayOfMonth);
      }
    });

    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String name = nameEditText.getText().toString();
        int quantity = 1;
        try {
          quantity = Integer.parseInt(quantityEditText.getText().toString());
        } catch (Exception e) {
          Log.e(TAG, "You failed to provide a quantity!");
        }
        mCurrentItem = new Item(name, quantity, calendar);
        if (!isEdit) {
          mItems.add(mCurrentItem);
        }
        Log.d(TAG, "items count = " + mItems.size());
        Log.d(TAG, "items = " + mItems);
        showCurrentItem();
      }
    });

    builder.create().show();
  }

  private void showCurrentItem() {
    mNameTextView.setText(mCurrentItem.getName());
    mQuantityTextView.setText(getString(R.string.quantity_format, mCurrentItem.getQuantity()));
    mDateTextView.setText(getString(R.string.date_format, mCurrentItem.getDeliveryDateString()));
  }


  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    getMenuInflater().inflate(R.menu.menu_context, menu);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_reset:
        mClearedItem = mCurrentItem;
//        mCurrentItem = new Item();
        mCurrentItem = Item.getEmptyItem();
        showCurrentItem();
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Item cleared", Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mCurrentItem = mClearedItem;
            mClearedItem = null;
            showCurrentItem();
            Snackbar.make(findViewById(R.id.coordinator_layout), "Item restored", Snackbar.LENGTH_LONG).show();
          }
        });
        snackbar.show();
        return true;
      case R.id.action_clear_all:
        showConfirmationDialog();
        return true;
      case R.id.action_search:
        showSearchDialog();
        return true;
      case R.id.action_settings:
        startActivity(new Intent(Settings.ACTION_SETTINGS));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_context_edit:
        addEditItem(true);
        return true;
      case R.id.menu_context_remove:
        mItems.remove(mCurrentItem);
        if (mItems.size() > 0) {
          mCurrentItem = mItems.get(mItems.size() - 1);
        } else {
          mCurrentItem = Item.getEmptyItem();
        }
        showCurrentItem();
        return true;
    }
    return super.onContextItemSelected(item);
  }

  private void showConfirmationDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.clear_all));
    builder.setMessage("Are you sure you want to remove all the items?  This cannot be undone");
    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mItems.clear();
        mCurrentItem = Item.getEmptyItem();
        showCurrentItem();
      }
    });
    builder.create().show();
  }

  private void showSearchDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.choose_an_item);
    builder.setItems(getNames(), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
       mCurrentItem = mItems.get(which);
       showCurrentItem();
      }
    });
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.create().show();

  }

  private String[] getNames() {
    String[] names = new String[mItems.size()];
    for (int i = 0; i < mItems.size(); i++) {
      names[i] = mItems.get(i).getName();
    }
    return names;
  }
}
