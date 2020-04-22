package stickyWallet.utils

object SQLStatements {

    fun CREATE_TABLE_CURRENCIES(prefix: String) = """
        CREATE TABLE IF NOT EXISTS ${prefix}_currencies(
            id INT PRIMARY KEY AUTO_INCREMENT,
            uuid VARCHAR(255),
            name_singular VARCHAR(255),
            name_plural VARCHAR(255),
            default_balance DECIMAL,
            symbol VARCHAR(10),
            decimals_supported INT,
            is_default INT,
            payable INT,
            color VARCHAR(255),
            exchange_rate DECIMAL,
            CONSTRAINT UniqueCurrency UNIQUE (name_singular, name_plural)
        );
    """.trimIndent()

    fun CREATE_TABLE_ACCOUNTS(prefix: String) = """
        CREATE TABLE IF NOT EXISTS ${prefix}_accounts(
            id INT PRIMARY KEY AUTO_INCREMENT,
            nickname VARCHAR(255),
            uuid VARCHAR(255),
            payable INT,
            CONSTRAINT UniqueAccount UNIQUE (uuid)
        );
    """.trimIndent()

    fun CREATE_TABLE_BALANCES(prefix: String) = """
        CREATE TABLE IF NOT EXISTS ${prefix}_balances(
            account_id  VARCHAR(255),
            currency_id VARCHAR(255),
            balance     DECIMAL,
            CONSTRAINT UniqueBalance UNIQUE (account_id, currency_id)
        );
    """.trimIndent()

    fun SELECT_ALL_CURRENCIES(prefix: String) = """
        SELECT * FROM ${prefix}_currencies;
    """.trimIndent()

    fun SELECT_CURRENCY_BY_ID(prefix: String) = """
        SELECT
        ${CurrencyParams.defaultBalance}, ${CurrencyParams.symbol}, ${CurrencyParams.decimalsSupported},
        ${CurrencyParams.defaultCurrency}, ${CurrencyParams.payable}, ${CurrencyParams.color},
        ${CurrencyParams.exchangeRate}
        FROM ${prefix}_currencies
        WHERE uuid = ?
        LIMIT 1;
    """.trimIndent()

    fun INSERT_CURRENCY(prefix: String) = """
        INSERT INTO ${prefix}_currencies (
            ${CurrencyParams.uuid},
            ${CurrencyParams.singular},
            ${CurrencyParams.plural},
            ${CurrencyParams.defaultBalance},
            ${CurrencyParams.symbol},
            ${CurrencyParams.decimalsSupported},
            ${CurrencyParams.defaultCurrency},
            ${CurrencyParams.payable},
            ${CurrencyParams.color},
            ${CurrencyParams.exchangeRate}
        )
        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY
        UPDATE
        ${CurrencyParams.uuid} = VALUES(${CurrencyParams.uuid}),
        ${CurrencyParams.singular} = VALUES(${CurrencyParams.singular}),
        ${CurrencyParams.plural} = VALUES(${CurrencyParams.plural}),
        ${CurrencyParams.defaultBalance} = VALUES(${CurrencyParams.defaultBalance}),
        ${CurrencyParams.symbol} = VALUES(${CurrencyParams.symbol}),
        ${CurrencyParams.decimalsSupported} = VALUES(${CurrencyParams.decimalsSupported}),
        ${CurrencyParams.defaultCurrency} = VALUES(${CurrencyParams.defaultCurrency}),
        ${CurrencyParams.payable} = VALUES(${CurrencyParams.payable}),
        ${CurrencyParams.color} = VALUES(${CurrencyParams.color}),
        ${CurrencyParams.exchangeRate} = VALUES(${CurrencyParams.exchangeRate});
    """.trimIndent()

    fun DELETE_CURRENCY_FROM_CURRENCIES(prefix: String) = """
        DELETE FROM ${prefix}_currencies WHERE uuid = ?;
    """.trimIndent()

    fun DELETE_BALANCES_WITH_CURRENCY_ID(prefix: String) = """
        DELETE FROM ${prefix}_balances WHERE currency_id = ?;
    """.trimIndent()

    fun SELECT_TOP_ACCOUNTS_BALANCES(prefix: String, offset: Int, amount: Int) = """
        SELECT
        ${BalanceParams.accountID}, ${BalanceParams.balance}
        FROM ${prefix}_balances WHERE currency_id = ?
        ORDER BY balance DESC LIMIT $offset, $amount;
    """.trimIndent()

    fun SELECT_TOP_ACCOUNT_NICKNAMES(prefix: String, amount: Int): String {
        val list = Array(amount) { "?" }
        return """
            SELECT
            ${AccountParams.nickname}, ${AccountParams.uuid}
            FROM ${prefix}_accounts
            WHERE uuid in (${list.joinToString()});
        """.trimIndent()
    }

    fun DELETE_ACCOUNT(prefix: String) = """
        DELETE FROM ${prefix}_accounts WHERE uuid = ?;
    """.trimIndent()

    fun DELETE_BALANCES_FOR_ACCOUNT(prefix: String) = """
        DELETE FROM ${prefix}_balances WHERE account_id = ?;
    """.trimIndent()

    fun INSERT_ACCOUNT_BALANCES(prefix: String, ignoreInsert: Boolean = false) = arrayOf(
        """
            INSERT INTO ${prefix}_accounts (${AccountParams.nickname}, ${AccountParams.uuid}, ${AccountParams.payable})
            VALUES(?, ?, ?)
            ON DUPLICATE KEY UPDATE
            ${AccountParams.nickname} = VALUES(${AccountParams.nickname}),
            ${AccountParams.uuid} = VALUES(${AccountParams.uuid}),
            ${AccountParams.payable} = VALUES(${AccountParams.payable});
        """.trimIndent(),
        """
            ${if (ignoreInsert) "INSERT IGNORE" else "INSERT"} INTO ${prefix}_balances (
                ${BalanceParams.accountID},
                ${BalanceParams.currencyID},
                ${BalanceParams.balance}
            )
            VALUES(?, ?, ?) ${if (ignoreInsert) "" else """
                ON DUPLICATE KEY UPDATE
                ${BalanceParams.balance} = VALUES(${BalanceParams.balance})
            """.trimIndent()};
        """.trimIndent()
    )

    fun SELECT_ACCOUNT(prefix: String, idKey: String) = """
        SELECT *
        FROM ${prefix}_accounts
        WHERE $idKey = ? LIMIT 1;
    """.trimIndent()

    fun SELECT_BALANCES_FOR_ACCOUNT(prefix: String) = """
        SELECT * FROM ${prefix}_balances WHERE account_id = ?;
    """.trimIndent()

    fun SELECT_ALL_ACCOUNT_IDS(prefix: String) = """
        SELECT ${AccountParams.uuid} FROM ${prefix}_accounts;
    """.trimIndent()

    fun SELECT_CONSTRAINT_FOR_TABLE(name: String) = """
        SELECT true
	    FROM information_schema.TABLE_CONSTRAINTS
	    WHERE
		    CONSTRAINT_SCHEMA = DATABASE() AND
		    CONSTRAINT_NAME   = '$name' AND
		    CONSTRAINT_TYPE   = "UNIQUE"
    """.trimIndent()

    fun ALTER_TABLE_ADD_UNIQUE_CONSTRAINT(table: String, uniqueConstraint: String) = """
        ALTER TABLE $table
        ADD CONSTRAINT $uniqueConstraint;
    """.trimIndent()

    object CurrencyParams {
        const val uuid = "uuid"
        const val singular = "name_singular"
        const val plural = "name_plural"
        const val defaultBalance = "default_balance"
        const val symbol = "symbol"
        const val decimalsSupported = "decimals_supported"
        const val defaultCurrency = "is_default"
        const val payable = "payable"
        const val color = "color"
        const val exchangeRate = "exchange_rate"
    }

    object AccountParams {
        const val nickname = "nickname"
        const val uuid = "uuid"
        const val payable = "payable"
    }

    object BalanceParams {
        const val accountID = "account_id"
        const val currencyID = "currency_id"
        const val balance = "balance"
    }
}