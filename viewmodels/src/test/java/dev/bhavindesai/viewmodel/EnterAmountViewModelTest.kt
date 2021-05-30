package dev.bhavindesai.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.viewmodel.utils.getOrAwaitValue
import org.junit.*
import org.junit.rules.TestRule

@SmallTest
class EnterAmountViewModelTest {

    @get:Rule var rule: TestRule = InstantTaskExecutorRule()
    private lateinit var viewModel: EnterAmountViewModel

    @Before
    fun setUp() {
        viewModel = EnterAmountViewModel()
    }

    @Test
    fun testButtonIsEnabledCorrectly() {
        // Initially disabled
        Assert.assertFalse(viewModel.enableConvertButton.getOrAwaitValue())

        // Still disabled as amount is not entered
        viewModel.onCurrencySelection(Currency("USD", "United States Dollar"))
        Assert.assertFalse(viewModel.enableConvertButton.getOrAwaitValue())

        // Still disabled as amount is zero
        viewModel.onNumpadClick("0")
        Assert.assertFalse(viewModel.enableConvertButton.getOrAwaitValue())

        // Should be enabled now
        viewModel.onNumpadClick("1")
        Assert.assertTrue(viewModel.enableConvertButton.getOrAwaitValue())
    }

    @Test
    fun testAmountDisplayedCorrectly() {

        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("1")
        viewModel.onDoubleZeroClick()

        Assert.assertEquals("1.00", viewModel.amount.getOrAwaitValue())
    }

    @Test
    fun testInitialAmount() {
        Assert.assertEquals("0.00", viewModel.amount.getOrAwaitValue())
    }

    @Test
    fun testClearAmount() {

        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("1")
        viewModel.onDoubleZeroClick()
        viewModel.onClearClick()

        Assert.assertEquals("0.00", viewModel.amount.getOrAwaitValue())
    }

    @Test
    @Ignore("TODO: Need to workout how to deal with LiveEvents")
    fun testConvertButtonClick() {
        viewModel.onCurrencySelection(Currency(
            "USD",
            "United States Dollar"
        ))

        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("0")
        viewModel.onNumpadClick("1")
        viewModel.onDoubleZeroClick()

        viewModel.onConvertClick()
        Assert.assertEquals(
            ("USD" to 100).toString(),
            viewModel.redirectToConvert.getOrAwaitValue().toString()
        )
    }
}