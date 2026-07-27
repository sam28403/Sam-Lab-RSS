package cc.samlab.rss.domain.service

import android.content.Context
import android.os.Looper
import androidx.datastore.preferences.core.intPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import cc.samlab.rss.R
import cc.samlab.rss.domain.model.account.Account
import cc.samlab.rss.domain.model.account.AccountType
import cc.samlab.rss.domain.model.feed.Feed
import cc.samlab.rss.domain.model.group.Group
import cc.samlab.rss.domain.repository.AccountDao
import cc.samlab.rss.domain.repository.ArticleDao
import cc.samlab.rss.domain.repository.FeedDao
import cc.samlab.rss.domain.repository.GroupDao
import cc.samlab.rss.infrastructure.di.ApplicationScope
import cc.samlab.rss.infrastructure.preference.SettingsProvider
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.getDefaultGroupId
import cc.samlab.rss.ui.ext.put
import cc.samlab.rss.ui.ext.showToast
import cc.samlab.rss.ui.ext.spacerDollar

class AccountService
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    settingsProvider: SettingsProvider,
) {

    private val accountIdKey = intPreferencesKey(DataStoreKey.currentAccountId)

    val currentAccountIdFlow =
        settingsProvider.preferencesFlow
            .map { it[accountIdKey] }
            .stateIn(scope = coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    val currentAccountFlow =
        currentAccountIdFlow
            .combine(getAccounts()) { id, accounts ->
                id?.let { accounts.firstOrNull { it.id == id } }
            }
            .stateIn(scope = coroutineScope, SharingStarted.Eagerly, initialValue = null)

    fun getAccounts(): Flow<List<Account>> = accountDao.queryAllAsFlow()

    fun getAccountFlowById(accountId: Int): Flow<Account?> = accountDao.queryAccount(accountId)

    suspend fun getAccountById(accountId: Int): Account? = accountDao.queryById(accountId)

    fun getCurrentAccount(): Account = runBlocking {
        currentAccountFlow.first { it != null } as Account
    }

    fun getCurrentAccountId(): Int = runBlocking {
        currentAccountIdFlow.first { it != null } as Int
    }

    suspend fun isNoAccount(): Boolean = accountDao.queryAll().isEmpty()

    suspend fun addAccount(account: Account): Account {
        val id = accountDao.insert(account).toInt()
        return account.copy(id = id).also {
            when (it.type) {
                AccountType.Local -> {
                    groupDao.insert(
                        Group(
                            id = it.id!!.getDefaultGroupId(),
                            name = context.getString(R.string.defaults),
                            accountId = it.id!!,
                        )
                    )
                }
            }
            context.dataStore.put(DataStoreKey.currentAccountId, it.id!!)
            context.dataStore.put(DataStoreKey.currentAccountType, it.type.id)
        }
    }

    private fun getDefaultAccount(): Account =
        Account(type = AccountType.Local, name = context.getString(R.string.read_you))

    private suspend fun addDefaultAccount(): Account = addAccount(getDefaultAccount())

    suspend fun initWithDefaultAccount() {
        val account = addDefaultAccount()
        val group = getDefaultGroup()
    }

    fun getDefaultGroup(): Group =
        getCurrentAccountId().let {
            Group(
                id = it.getDefaultGroupId(),
                name = context.getString(R.string.defaults),
                accountId = it,
            )
        }

    suspend fun update(accountId: Int, block: Account.() -> Account) {
        accountDao.queryById(accountId)?.let { accountDao.update(it.run(block)) }
    }

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(accountId: Int) {
        if (accountDao.queryAll().size == 1) {
            Looper.myLooper() ?: Looper.prepare()
            context.showToast(context.getString(R.string.must_have_an_account))
            Looper.loop()
            return
        }
        accountDao.queryById(accountId)?.let {
            articleDao.deleteByAccountId(accountId)
            feedDao.deleteByAccountId(accountId)
            groupDao.deleteByAccountId(accountId)
            accountDao.delete(it)
            accountDao.queryAll().getOrNull(0)?.let {
                context.dataStore.put(DataStoreKey.currentAccountId, it.id!!)
                context.dataStore.put(DataStoreKey.currentAccountType, it.type.id)
            }
        }
    }

    suspend fun switch(account: Account) {
        context.dataStore.put(DataStoreKey.currentAccountId, account.id!!)
        context.dataStore.put(DataStoreKey.currentAccountType, account.type.id)
    }
}
