/*
 *    Copyright 2019 Fabien Boismoreau
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bwaim.cryptocurrency.features.cryptocurrency.ui

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.PagedList
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.executor.PostExecutionThread
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.CryptoCurrencyRepository
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.Listing
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.NetworkState
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class CryptoCurrenciesFragmentTest {

    //<editor-fold desc="Init">
    companion object {
        private const val PAGE_SIZE = 20
    }

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockNavController: NavController
    @Mock
    private lateinit var mockViewModelFactory: ViewModelProvider.Factory

    // Mock for the ViewModel
    @Mock
    private lateinit var mockCryptoCurrencyRepository: CryptoCurrencyRepository
    @Mock
    private lateinit var mockPostExecutionThread: PostExecutionThread
    @Mock
    private lateinit var mockPagedList: LiveData<PagedList<CryptoCurrency>>
    @Mock
    private lateinit var mockNetworkState: MutableLiveData<NetworkState>
    @Mock
    private lateinit var mockRefreshState: LiveData<NetworkState>
    @Mock
    private lateinit var mockRefresh: (withFeedback: Boolean) -> Unit
    @Mock
    private lateinit var mockRetry: () -> Unit

    private lateinit var cryptoCurrenciesScenario: FragmentScenario<CryptoCurrenciesFragment>
    private var cryptoCurrenciesViewModel: CryptoCurrenciesViewModel? = null
    private lateinit var testScheduler: TestScheduler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }
    //</editor-fold>

    //<editor-fold desc="Init State">
    @Test
    fun initStateAuthenticated() {
        val listing = Listing(mockPagedList, mockNetworkState, mockRefreshState, mockRefresh, mockRetry)
        given(mockCryptoCurrencyRepository.getCryptoCurrencies(PAGE_SIZE)).willReturn(listing)
        given(mockPostExecutionThread.getScheduler()).willReturn(testScheduler)

        cryptoCurrenciesViewModel = CryptoCurrenciesViewModel(mockCryptoCurrencyRepository, mockPostExecutionThread)

        openCryptoCurrenciesFragment()

        onView(withId(R.id.rv_crypto_currencies)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun loadingData() {
        val networkState = MutableLiveData<NetworkState>()
        val listing = Listing(mockPagedList, networkState, mockRefreshState, mockRefresh, mockRetry)
        given(mockCryptoCurrencyRepository.getCryptoCurrencies(PAGE_SIZE)).willReturn(listing)
        given(mockPostExecutionThread.getScheduler()).willReturn(testScheduler)

        cryptoCurrenciesViewModel = CryptoCurrenciesViewModel(mockCryptoCurrencyRepository, mockPostExecutionThread)

        openCryptoCurrenciesFragment()

        networkState.value = NetworkState.LOADING

        onView(withId(R.id.progress_bar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
    //</editor-fold>

    //<editor-fold desc="Init fragment">
    private fun openCryptoCurrenciesFragment() {
        openCryptoCurrenciesFragment(null)
    }

    private fun openCryptoCurrenciesFragment(args: Bundle?) {
        `when`<ViewModel>(mockViewModelFactory.create(any()))
            .thenReturn(cryptoCurrenciesViewModel)

        cryptoCurrenciesScenario = FragmentScenario
            .launchInContainer(CryptoCurrenciesFragment::class.java, args, R.style.AppTheme,
                object : FragmentFactory() {
                    override fun instantiate(
                        classLoader: ClassLoader,
                        className: String
                    ): Fragment {
                        val cryptoCurrenciesFragment = CryptoCurrenciesFragment()
                        cryptoCurrenciesFragment.viewLifecycleOwnerLiveData.observeForever { lifecycleOwner ->
                            cryptoCurrenciesFragment.viewModelFactory = mockViewModelFactory
                            if (lifecycleOwner != null) {
                                Navigation.setViewNavController(
                                    cryptoCurrenciesFragment.requireView(),
                                    mockNavController
                                )
                            }
                        }
                        return cryptoCurrenciesFragment
                    }
                })
    }
    //</editor-fold>
}