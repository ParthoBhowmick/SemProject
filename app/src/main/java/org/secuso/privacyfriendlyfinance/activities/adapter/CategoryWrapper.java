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

package org.secuso.privacyfriendlyfinance.activities.adapter;

import android.arch.lifecycle.LiveData;

import org.secuso.privacyfriendlyfinance.domain.FinanceDatabase;
import org.secuso.privacyfriendlyfinance.domain.model.Category;

/**
 * Wrapper class for category.
 *
 * @author Felix Hofmann
 * @author Leonard Otto
 */
public class CategoryWrapper implements IdProvider {
    private Category category;
    private LiveData<Long> balance;

    public CategoryWrapper(Category category) {
        this.category = category;
        balance = FinanceDatabase.getInstance().transactionDao().sumForCategoryThisMonth(category.getId());
    }

    public LiveData<Long> getBalance() {
        return balance;
    }

    @Override
    public Long getId() {
        return category.getId();
    }

    public Category getCategory() {
        return category;
    }
}
