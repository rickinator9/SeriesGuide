package com.battlelancer.seriesguide.sync;

import static org.junit.Assert.*;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class AccountUtilsTest {

    private Account[] noAccounts;
    private Account[] oneAccount;
    private Account[] multipleAccounts;

    @Before
    public void initAccountArrays() {
        noAccounts = new Account[0];
        oneAccount = new Account[] { new Account("", "")};
        multipleAccounts = new Account[] { new Account("", ""), new Account("", "")};
    }

    @Test
    public void test_isAccountExists_noAccounts() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(noAccounts);

        assertEquals(false, AccountUtils.isAccountExists(manager));
    }

    @Test
    public void test_isAccountExists_oneAccount() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(oneAccount);

        assertEquals(true, AccountUtils.isAccountExists(manager));
    }

    @Test
    public void test_isAccountExists_multipleAccounts() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(multipleAccounts);

        assertEquals(true, AccountUtils.isAccountExists(manager));
    }

    @Test
    public void test_getAccount_noAccounts() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(noAccounts);

        assertNull(AccountUtils.getAccount(manager));
    }

    @Test
    public void test_getAccount_oneAccount() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(oneAccount);

        assertEquals(true, oneAccount[0] == AccountUtils.getAccount(manager));
    }

    @Test
    public void test_getAccount_multipleAccounts() {
        AccountManager manager = Mockito.mock(AccountManager.class);
        Mockito.when(manager.getAccountsByType(AccountUtils.ACCOUNT_TYPE)).thenReturn(multipleAccounts);

        Account account = AccountUtils.getAccount(manager);
        boolean isAccountFound = false;
        for(Account acc : multipleAccounts) {
            if(acc == account) {
                isAccountFound = true;
                break;
            }
        }

        assertEquals(true, isAccountFound);
    }
}