# OptionBuyerAndroid

Fresh Android app built separately from earlier repositories.

Current APK build contains the supplied option buyer decision rules in a manual calculator form:

* Indicator mode: RSI above 50 gives BUY CE, RSI below 50 gives BUY PE, RSI equal to 50 gives WAIT.
* Level breakout mode: spot above upper level gives BUY CE, spot below lower level gives BUY PE, otherwise WAIT.
* Strike selection follows the supplied logic: BANKNIFTY rounds to 100, NIFTY and FINNIFTY round to 50. OTM 0 means ATM, positive means OTM and negative means ITM.

Live broker market data and live order placement are not enabled in this first installable build.
