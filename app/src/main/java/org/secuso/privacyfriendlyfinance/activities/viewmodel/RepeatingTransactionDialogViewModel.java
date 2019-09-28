/*
 Privacy Friendly Finance Manager is licensed under the GPLv3.
 Copyright (C) 2019 Leonard Otto, Felix Hofmann

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 General Public License as published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program.
 If not, see http://www.gnu.org/licenses/.

 Additionally icons from Google Design Material Icons are used that are licensed under Apache
 License Version 2.0.
 */

package org.secuso.privacyfriendlyfinance.activities.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import org.joda.time.LocalDate;
import org.secuso.privacyfriendlyfinance.BR;
import org.secuso.privacyfriendlyfinance.activities.adapter.IdProvider;
import org.secuso.privacyfriendlyfinance.domain.FinanceDatabase;
import org.secuso.privacyfriendlyfinance.domain.access.AccountDao;
import org.secuso.privacyfriendlyfinance.domain.access.CategoryDao;
import org.secuso.privacyfriendlyfinance.domain.access.RepeatingTransactionDao;
import org.secuso.privacyfriendlyfinance.domain.model.Account;
import org.secuso.privacyfriendlyfinance.domain.model.Category;
import org.secuso.privacyfriendlyfinance.domain.model.RepeatingTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * View model for the repeating transaction dialog.
 *
 * @author Felix Hofmann
 * @author Leonard Otto
 */
public class RepeatingTransactionDialogViewModel extends CurrencyInputBindableViewModel {
    private CategoryDao categoryDao = FinanceDatabase.getInstance().categoryDao();
    private AccountDao accountDao = FinanceDatabase.getInstance().accountDao();
    private RepeatingTransactionDao transactionDao = FinanceDatabase.getInstance().repeatingTransactionDao();

    private LiveData<List<Category>> categories;
    private LiveData<List<Account>> accounts = accountDao.getAll();

    private LiveData<RepeatingTransaction> transactionLive;
    private RepeatingTransaction transaction;

    private Application application;
    private boolean amountEdited = false;

    private long transactionId = -1;

    public RepeatingTransactionDialogViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        categories = Transformations.map(categoryDao.getAll(), new Function<List<Category>, List<Category>>() {
            @Override
            public List<Category> apply(List<Category> input) {
                List<Category> categoriesAndVoid = new ArrayList<>();
                categoriesAndVoid.add(null);
                categoriesAndVoid.addAll(input);
                return categoriesAndVoid;
            }
        });

        Transformations.map(accounts, new Function<List<Account>, Void>() {
            @Override
            public Void apply(List<Account> input) {
                notifyPropertyChanged(BR.accountIndex);
                return null;
            }
        });

        setTransactionDummy();
    }

    @Override
    protected Long getNumericAmount() {
        if (transaction == null) return null;
        if (transaction.getId() == null && !amountEdited) return null;
        return transaction.getAmount();
    }

    @Override
    protected void setNumericAmount(Long amount) {
        amountEdited = true;
        if (amount == null) amount = 0L;
        transaction.setAmount(amount);
    }

    public LiveData<List<Category>> getAllCategories() {
        return categories;
    }
    public LiveData<List<Account>> getAllAccounts() {
        return accounts;
    }

    public LiveData<RepeatingTransaction> setTransactionId(long transactionId) {
        if (this.transactionId != transactionId) {
            this.transactionId = transactionId;
            if (transactionId == -1) {
                setTransactionDummy();
            } else {
                transactionLive = transactionDao.get(transactionId);
            }
        }
        return transactionLive;
    }

    private void setTransactionDummy() {
        MutableLiveData<RepeatingTransaction> mutableTransaction = new MutableLiveData<>();
        mutableTransaction.postValue(new RepeatingTransaction());
        transactionLive = mutableTransaction;
    }


    private String originalName;
    private Long originalAccountId;
    private Long originalCategoryId;
    private Long originalAmount;
    private LocalDate originalEnd;
    private boolean originalWeelky;
    private Long originalInterval;
    public RepeatingTransaction getTransaction() {
        return transaction;
    }
    public void setTransaction(RepeatingTransaction transaction) {
        this.transaction = transaction;
        originalName = transaction.getName();
        originalAccountId = transaction.getAccountId();
        originalCategoryId = transaction.getCategoryId();
        originalAmount = transaction.getAmount();
        originalEnd = transaction.getEnd();
        originalWeelky = transaction.isWeekly();
        originalInterval = transaction.getInterval();
        notifyChange();
    }

    @Bindable
    public String getName() {
        return transaction.getName();
    }
    public void setName(String name) {
        if (name == null) name = "";
        if (transaction.getName() == null) transaction.setName("");
        if (!transaction.getName().equals(name)) {
            transaction.setName(name);
            notifyPropertyChanged(BR.name);
        }
    }

    @Bindable
    public int getEndSet() {
        return getEnd() == null ? View.INVISIBLE : View.VISIBLE;
    }

    @Bindable
    public String getEndString() {
        if (transaction.getEnd() == null) return null;
        return transaction.getEnd().toString();
    }
    public LocalDate getEnd() {
        return transaction.getEnd();
    }
    public void clearEnd() {
        setEnd(null);
    }
    public void setEnd(LocalDate date) {
        Log.d("DateSet", "" + date);
        if (date != null) {
            if (transaction.getEnd() == null || !transaction.getEnd().equals(date)) {
                transaction.setEnd(date);
                notifyPropertyChanged(BR.endString);
                notifyPropertyChanged(BR.endSet);
            }
        } else if (transaction.getEnd() != null) {
            transaction.setEnd(null);
            notifyPropertyChanged(BR.endString);
            notifyPropertyChanged(BR.endSet);
        }
    }

    private int indexOfId(List<? extends IdProvider> list, Long id) {
        if (list == null) return 0;
        for (int i = 0; i < list.size(); ++i) {
            IdProvider element = list.get(i);
            if ((element != null && element.getId() == id) || (element == null && id == null)) {
                return i;
            }
        }
        return 0;
    }

    @Bindable
    public int getAccountIndex() {
        if (accounts.getValue() == null) return 0;
        return indexOfId(accounts.getValue(), transaction.getAccountId());
    }
    public void setAccountIndex(int accountIndex) {
        Log.d("accountIndex", "" + accountIndex);
        Account account = accounts.getValue().get(accountIndex);
        if (transaction.getAccountId() != account.getId()) {
            transaction.setAccountId(account.getId());
            notifyPropertyChanged(BR.accountIndex);
        }
    }

    @Bindable
    public boolean getWeekly() {
        return transaction.isWeekly();
    }
    public void setWeekly(boolean checked) {
        if (transaction.isWeekly() != checked) {
            transaction.setWeekly(checked);
            notifyPropertyChanged(BR.weekly);
        }
    }

    @Bindable
    public String getInterval() {
        return String.valueOf(transaction.getInterval());
    }
    public void setInterval(String str) {
        try {
            long interval = Long.parseLong(str);
            if (transaction.getInterval() != interval) {
                transaction.setInterval(interval);
                notifyPropertyChanged(BR.interval);
            }
        } catch (NumberFormatException e) {

        }
    }

    @Bindable
    public int getCategoryIndex() {
        if (categories.getValue() == null) return 0;
        return indexOfId(categories.getValue(), transaction.getCategoryId());
    }
    public void setCategoryIndex(int categoryIndex) {
        Log.d("categoryIndex", "" + categoryIndex);
        Category category = categories.getValue().get(categoryIndex);
        if (category == null) {
            if (transaction.getCategoryId() != null) {
                transaction.setCategoryId(null);
                notifyPropertyChanged(BR.categoryIndex);
            }
        } else if (transaction.getCategoryId() != category.getId()) {
            transaction.setCategoryId(category.getId());
            notifyPropertyChanged(BR.categoryIndex);
        }
    }


    public void submit() {
        transactionDao.updateOrInsertAsync(transaction);
    }

    public void cancel() {
        transaction.setName(originalName);
        transaction.setAccountId(originalAccountId);
        transaction.setCategoryId(originalCategoryId);
        transaction.setAmount(originalAmount);
        transaction.setEnd(originalEnd);
        transaction.setWeekly(originalWeelky);
        transaction.setInterval(originalInterval);
    }
}