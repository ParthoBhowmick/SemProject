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
import android.databinding.Bindable;
import android.graphics.Color;
import android.support.annotation.NonNull;

import org.secuso.privacyfriendlyfinance.BR;
import org.secuso.privacyfriendlyfinance.helpers.CurrencyHelper;

/**
 * Bindable view model for currency input.
 *
 * @author Felix Hofmann
 * @author Leonard Otto
 */
public abstract class CurrencyInputBindableViewModel extends BindableViewModel {
    private int positiveColor = Color.GREEN;
    private int negativeColor = Color.RED;
    private String amountString = null;

    public CurrencyInputBindableViewModel(@NonNull Application application) {
        super(application);
    }

    @Bindable
    public String getAmountString() {
        if (amountString == null) {
            amountString = CurrencyHelper.convertToString(getNumericAmount());
            if (amountString == null) {
                amountString = "";
            }
        }
        return amountString;
    }
    public void setAmountString(String amountString) {
        if (amountString == null) amountString = "";
        this.amountString = amountString;
        Long number = CurrencyHelper.convertToLong(amountString);
        if (number != null) {
            if (getNumericAmount() != number) {
                setNumericAmount(number);
                notifyPropertyChanged(BR.expense);
                notifyPropertyChanged(BR.amountColor);
            }
        } else {
            setNumericAmount(null);
            notifyPropertyChanged(BR.expense);
            notifyPropertyChanged(BR.amountColor);
        }

    }

    @Bindable
    public int getAmountColor() {
        return getExpense() ? negativeColor : positiveColor;
    }

    @Bindable
    public boolean getExpense() {
        return getAmountString().startsWith("-");
    }

    public void setExpense(boolean checked) {
        if (getExpense() != checked) {
            if (checked) {
                setAmountString("-" + getAmountString());
            } else {
                setAmountString(getAmountString().substring(1));
            }
            notifyPropertyChanged(BR.amountString);
        }
//        if (getNumericAmount() == null) {
//            setAmountString(checked ? "-" + getAmountString() : "");
//            notifyPropertyChanged(BR.amountColor);
//            notifyPropertyChanged(BR.amountString);
//        } else if ((getNumericAmount() > 0 && checked) || (getNumericAmount() < 0 && !checked)) {
//            setAmountString(CurrencyHelper.convertToString(getNumericAmount() * -1));
//            notifyPropertyChanged(BR.expense);
//            notifyPropertyChanged(BR.amountColor);
//            notifyPropertyChanged(BR.amountString);
//        }
    }

    protected abstract Long getNumericAmount();
    protected abstract void setNumericAmount(Long amount);

    public void setCurrencyColors(int positiveColor, int negativeColor) {
        this.positiveColor = positiveColor;
        this.negativeColor = negativeColor;
        notifyPropertyChanged(BR.amountColor);
    }
}
