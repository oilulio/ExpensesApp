package com.github.oilulio.expensesapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.oilulio.expensesapp.ui.theme.ExpensesTheme
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), LifecycleObserver {

  /*  Tests :
    New claim populates DB correctly
    Screen rotation works and does not lose data
    Cancel confirm works (both options)
    Database population regenerates view and total
    Validation of input date and amount
    Invalid amount red while typing
    Stored image for receipt in data/data/[app]/files/receipts
    Actual use on tablet
  */
  private val claimViewModel: ClaimViewModel by viewModels {
    ClaimViewModelFactory((application as ExpensesApplication).repository)
  }

  private val startForNewClaimResult = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result: ActivityResult ->
    if (result.resultCode == Activity.RESULT_OK) {
      //  Won't get here if claim was cancelled
      val data = result.data
      claimViewModel.insert(
        Claim(
          date = data!!.getStringExtra("date")!!, // We enforced a date in NewClaim
          reason = data.getStringExtra("reason") ?: "Unspecified", // A default makes sense here
          paid = data.getBooleanExtra(
            "paid",
            false
          ), // Should not need default as boolean was set ...
          amount = data.getFloatExtra(
            "amount",
            0.0f
          ), // We enforced an amount (which could be zero)
          receiptUri = data.getStringExtra("receiptUri") ?: "No receipt",
        )
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.hide()

    // Testing only

    // Testing - checks database insert and the ability for the activity
    // display to update on database change by adding at fixed rate
    /*
        Timer().scheduleAtFixedRate(object : TimerTask() {
          override fun run() {
            claimViewModel.insert(
              Claim(
                date = "01/01/2024",
                amount = 10.11f,
                reason = "Reason",
                paid = Random.nextBoolean(),
                receiptUri="No receipt",
              )
            )
          }
        },1000,5000) // Add claim every 5s
    */
    setContent {
      ExpensesTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ExpensesView(claimViewModel)
        }
      }
    }
  }

  @Composable
  fun ExpenseItem(claim: Claim) {
    var reason = claim.reason
    if (reason.length > 12) reason = reason.substring(0, 12) + "..." // Summarise

    Row {
      Text(
        /*"%4d".format(claim.uid) + ":\t"+*/ // Could number, but wastes space
        DecimalFormat("£#,##0.00").format(claim.amount) + " on " + claim.date + " for " + reason
      )
      Text(if (claim.paid) " \u2713" else "") // Unicode tick if paid
    }
  }

  @Composable
  fun ExpensesView(
    claimViewModel: ClaimViewModel,
    modifier: Modifier = Modifier
  ) {
    // Activity that shows total; scrollable set of expense items; and New Claim button

    val list = claimViewModel.allClaims.collectAsStateWithLifecycle(initialValue = emptyList())
    val total by mutableStateOf(
      (claimViewModel.total.collectAsStateWithLifecycle(
        initialValue = Total(
          null
        )
      ).value).dbTotal
    )
    val unpaidTotal by mutableStateOf(
      (claimViewModel.unpaidTotal.collectAsStateWithLifecycle(
        initialValue = Unpaid(null)
      ).value).unpaidTotal
    )
    // In emulator, these do not seem to all work reliably, but they work on the device

    @Composable
    fun AddClaimButton(onClick: () -> Unit) {
      val context = LocalContext.current
      val intent = Intent(context, NewClaimActivity()::class.java)
      Button(onClick = {
        startForNewClaimResult.launch(intent)
      }) {
        Text("Add claim")
      }
    }
    Column {
      Row {
        Image(
          painter = painterResource(R.drawable.coins),
          contentDescription = "Context image",
          modifier = Modifier
            .background(Color.Blue)
            .size(125.dp)
            .padding(12.dp)
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Text(
          "Expenses total is " + if (total != null) DecimalFormat("£#,##0.00").format(total) else {
            "Unknown"
          }
              + ", Unpaid is " + if (unpaidTotal != null) DecimalFormat("£#,##0.00").format(
            unpaidTotal
          ) else {
            "Unknown"
          },
          style = MaterialTheme.typography.titleLarge,
          color = Color.Yellow,
          fontSize = 4.em,
          lineHeight = 40.sp,
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Blue)
            .size(125.dp)
            .padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))
      /* Alternative total presentation
      Box(
        modifier =
        Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Total is "+if (total!=null) DecimalFormat("£#,##0.00").format(total) else { "Unknown" }
          +", Unpaid is "+if (unpaidTotal!=null) DecimalFormat("£#,##0.00").format(unpaidTotal) else { "Unknown" },
          modifier = modifier
            .padding(10.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RectangleShape)
            .padding(10.dp)
          // Padding both inside and outside box
        )
      } */
      Column(
        modifier = Modifier
//          .verticalScroll(rememberScrollState()) // Expenses lines will scroll, total & New item won't.
          .weight(weight = 1f, fill = false)
      ) {

        LazyColumn(
          modifier = Modifier
//            .verticalScroll(rememberScrollState()) // Expenses lines will scroll, total & New item won't.
            .weight(weight = 1f, fill = false)
        ) {
          itemsIndexed(list.value) { _, item -> ExpenseItem(item) }
        }

        Box(
          modifier =
          Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center,
        ) {
          AddClaimButton {
          }
        }
      }
    }
  }
}
