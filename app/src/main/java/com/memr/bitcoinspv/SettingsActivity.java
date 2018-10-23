package com.memr.bitcoinspv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.memr.bitcoinspv.Dialogs.PasswordDialog;
import com.memr.bitcoinspv.Managers.ExchangeManager;
import com.memr.bitcoinspv.Managers.PasswordManager;
import com.memr.bitcoinspv.Util.FingerprintHelper;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.utils.ExchangeRate;

public class SettingsActivity extends BackUpWalletActivity {

    private static String PASSWORD_DIALOG_TAG = "passwordDialog";

    private static String ABOUT_PAGE_URL = "https://vericoin.info/about/";

    private MyPreferenceFragment fragment;

    public static Intent createIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onWalletKitReady() {

        if (fragment == null) {
            fragment = new MyPreferenceFragment();
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    protected void onWalletKitStop() {
        getFragmentManager().beginTransaction().remove(fragment).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements ExchangeManager.OnExchangeRateChange {

        private Preference changePasswordRow;
        private Preference exportWallet;
        private Preference viewSeed;
        private Preference deleteWallet;
        private Preference viewContacts;
        private CheckBoxPreference lockTransactions;
        private CheckBoxPreference fingerPrint;
        private BitcoinSPV bitcoinSPV;
        private PasswordManager passwordManager;
        private ExchangeManager exchangeManager;
        private FingerprintHelper fingerprintHelper;
        private PreferenceCategory securityCategory;
        private CheckBoxPreference secureWindow;
        private WalletAppKit kit;
        private SettingsActivity settingsActivity;
        private Preference aboutPage;

        private Preference refreshFiat;
        private ListPreference fiatType;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(com.memr.bitcoinspv.R.xml.preference_screen);
            bitcoinSPV = (BitcoinSPV) getActivity().getApplication();
            passwordManager = bitcoinSPV.getPasswordManager();
            exchangeManager = bitcoinSPV.getExchangeManager();
            exchangeManager.addExchangeRateChangeListener(this);

            settingsActivity = (SettingsActivity) getActivity();
            kit = settingsActivity.kit;

            changePasswordRow = findPreference(getString(com.memr.bitcoinspv.R.string.change_password_button));
            changePasswordRow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(ChangePasswordActivity.createIntent(getActivity()));
                    return true;
                }
            });

            exportWallet = findPreference(getString(com.memr.bitcoinspv.R.string.export_wallet_button));

            deleteWallet = findPreference(getString(com.memr.bitcoinspv.R.string.delete_wallet_button));

            securityCategory = (PreferenceCategory) findPreference(getString(com.memr.bitcoinspv.R.string.security_key));

            lockTransactions = (CheckBoxPreference) findPreference(getString(com.memr.bitcoinspv.R.string.lock_transactions_key));

            fingerPrint = (CheckBoxPreference) findPreference(getString(com.memr.bitcoinspv.R.string.fingerprint_enabled_key));

            secureWindow = (CheckBoxPreference) findPreference(getString(com.memr.bitcoinspv.R.string.secure_window_key));

            aboutPage = findPreference("aboutKey");

            aboutPage.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openAboutPage();
                    return true;
                }
            });

            fingerprintHelper = new FingerprintHelper((AppCompatActivity) getActivity());

            if (!fingerprintHelper.isFingerPrintSupported()) { //Device doesn't support fingerprint remove preference
                securityCategory.removePreference(fingerPrint);
            }

            viewSeed = findPreference(getString(com.memr.bitcoinspv.R.string.view_seed_button));

            viewContacts = findPreference(getString(com.memr.bitcoinspv.R.string.view_contacts_key));
            viewContacts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(ContactListActivity.getIntent(getActivity()));
                    return true;
                }
            });

            refreshFiat = findPreference("refreshFiat");
            fiatType = (ListPreference) findPreference("fiatType");

            fiatType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    exchangeManager.updateExchangeRate(newValue.toString());
                    return true;
                }
            });

            refreshFiat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), com.memr.bitcoinspv.R.string.refreshing_exchange_rate, Toast.LENGTH_LONG).show();
                    exchangeManager.downloadExchangeRateList(getActivity());
                    return true;
                }
            });

            updateExchangeRate(exchangeManager.getExchangeRate());
        }

        public void updateExchangeRate(ExchangeRate exchangeRate){
            fiatType.setSummary(exchangeRate.coin.toFriendlyString() + " = " + exchangeRate.fiat.toFriendlyString() + "\n"
                                + getString(com.memr.bitcoinspv.R.string.provided_by_coin_gecko));
        }

        public void deleteWallet() {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Are you sure you want to delete your wallet? This action can not be undone.")
                    .setTitle("Delete Wallet")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Do nothing
                        }
                    })
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(DeleteWalletActivity.createIntent(getActivity()));
                        }
                    });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void changeCheckBoxUsingPassword(final CheckBoxPreference checkBoxPreference) {
            final boolean after = checkBoxPreference.isChecked();
            checkBoxPreference.setChecked(!after); //Prevent any change before password

            PasswordDialog passwordDialog = new PasswordDialog();
            passwordDialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), PASSWORD_DIALOG_TAG);

            passwordDialog.setListener(new PasswordDialog.OnPasswordListener() {
                @Override
                public void onSuccess(String password) {
                    checkBoxPreference.setChecked(after);
                }
            });
        }

        public void openAboutPage(){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ABOUT_PAGE_URL));
            startActivity(browserIntent);
        }

        public boolean doesPasswordExist() {
            return passwordManager.doesPasswordExist();
        }

        public void openViewSeedActivity(String password) {
            startActivity(ViewSeedActivity.createIntent(getActivity(), password));
        }

        // Here are some examples of how you might call this method.
        // The first parameter is the MIME type, and the second parameter is the name
        // of the file you are creating:
        //
        // createFile("text/plain", "foobar.txt");
        // createFile("image/png", "mypicture.png");

        @Override
        public void onResume() {
            super.onResume();

            if (doesPasswordExist()) { //Require password before changing these settings.
                lockTransactions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                        changeCheckBoxUsingPassword(checkBoxPreference);
                        return true;
                    }
                });
                secureWindow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                        changeCheckBoxUsingPassword(checkBoxPreference);
                        return true;
                    }
                });
                fingerPrint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                        changeCheckBoxUsingPassword(checkBoxPreference);
                        return true;
                    }
                });
                viewSeed.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PasswordDialog passwordDialog = new PasswordDialog();
                        passwordDialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), PASSWORD_DIALOG_TAG);
                        passwordDialog.setListener(new PasswordDialog.OnPasswordListener() {
                            @Override
                            public void onSuccess(String password) {
                                openViewSeedActivity(password);
                            }

                        });
                        return true;
                    }
                });
                exportWallet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PasswordDialog dialog = new PasswordDialog();
                        dialog.setListener(new PasswordDialog.OnPasswordListener() {
                            @Override
                            public void onSuccess(String password) {
                                settingsActivity.backUpWallet();
                            }
                        });
                        dialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), PASSWORD_DIALOG_TAG);
                        return true;
                    }
                });
                deleteWallet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PasswordDialog dialog = new PasswordDialog();
                        dialog.setListener(new PasswordDialog.OnPasswordListener() {
                            @Override
                            public void onSuccess(String password) {
                                deleteWallet();
                            }
                        });
                        dialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), PASSWORD_DIALOG_TAG);
                        return true;
                    }
                });
            } else {
                exportWallet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        settingsActivity.backUpWallet();
                        return true;
                    }
                });
                deleteWallet.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        deleteWallet();
                        return true;
                    }
                });
                viewSeed.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        openViewSeedActivity("");
                        return true;
                    }
                });
                lockTransactions.setOnPreferenceClickListener(null);
                secureWindow.setOnPreferenceClickListener(null);
                fingerPrint.setOnPreferenceClickListener(null);
            }

            if(kit.wallet().isEncrypted()) {
                exportWallet.setSummary(com.memr.bitcoinspv.R.string.wallet_is_encrypted);
            }else{
                exportWallet.setSummary(com.memr.bitcoinspv.R.string.wallet_is_not_encrypted);
            }

            if (kit.wallet().isEncrypted()) { //Wallet is encrypted and there is a password.
                lockTransactions.setEnabled(false);
                fingerPrint.setEnabled(true);
            } else if (doesPasswordExist()) { //Wallet is NOT encrypted and there is a password.
                lockTransactions.setEnabled(true);
                fingerPrint.setEnabled(true);
            } else {                          //Wallet is NOT encrypted and there is NO password.
                lockTransactions.setEnabled(false);
                fingerPrint.setEnabled(false);
            }
        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            exchangeManager.removeExchangeRateChangeListener(this);
        }

        @Override
        public void exchangeRateUpdated(ExchangeRate exchangeRate) {
            updateExchangeRate(exchangeRate);
        }
    }
}
