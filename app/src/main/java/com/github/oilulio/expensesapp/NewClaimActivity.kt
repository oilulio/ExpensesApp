package com.github.oilulio.expensesapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.oilulio.expensesapp.ui.theme.ExpensesTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
class NewClaimActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.hide()
    setContent {
      ExpensesTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          NewClaimActivity()
        }
      }
    }
  }

  private fun uriForImageCapture(): Uri? {
    val outputFileUri: Uri?
    val directory = File(this.filesDir, "receipts")
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val file = File(
      directory, "Rcpt-${
        Date().toString().replace(" ", "_")
          .replace(":", "")
      }.png"
    )
    outputFileUri = MyFileProvider.getUriForFile(
      this,
      getApplicationContext().getPackageName() + ".provider",
      file
    )
    return outputFileUri
  }

  private fun futureDate(testDate: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    val strDate: Date? = sdf.parse(testDate)
    return (!Date().after(strDate))
  }

  @OptIn(ExperimentalMaterial3AdaptiveApi::class)
  @Composable
  fun NewClaimActivity(modifier: Modifier = Modifier) {

    var claimDate by rememberSaveable { mutableStateOf("") }
    var claimReason by rememberSaveable { mutableStateOf("") }
    var claimPaid by rememberSaveable { mutableStateOf(false) }
    var claimAmount by rememberSaveable { mutableStateOf("0.00") }
    var claimReceiptExists by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var validAmount by rememberSaveable { mutableStateOf(true) } // Zero is valid
    var validDate by rememberSaveable { mutableStateOf(false) } // Unspecified (i.e. "") in invalid
    var changesMade by rememberSaveable { mutableStateOf(false) } // Cancel is safe if no changes made
    var imageTaken by rememberSaveable { mutableStateOf(false) } //

    val backHandlingEnabled by remember { mutableStateOf(true) }

    var showConfirm by rememberSaveable { mutableStateOf(false) }

    val claimUri =
      uriForImageCapture()  // Stays same throughout Activity, so new receipt overwrites, which is good

    @Composable
    fun ConfirmCancel(
      show: Boolean = showConfirm,
      onDismissRequest: () -> Unit,
      onConfirmation: () -> Unit,
      dialogTitle: String,
      dialogText: String,
      icon: ImageVector,
    ) {
      if (show) {
        AlertDialog(
          icon = {
            Icon(icon, contentDescription = "Warning Icon")
          },
          title = {
            Text(text = "Confirm Cancel")
          },
          text = {
            Text(text = "Are you sure you want to cancel this claim - data will be lost?")
          },
          onDismissRequest = {
            onDismissRequest()
          },
          confirmButton = {
            TextButton(
              onClick = {
                onConfirmation()
              }
            ) {
              Text("Confirm")
            }
          },
          dismissButton = {
            TextButton(
              onClick = {
                onDismissRequest()
              }
            ) {
              Text("Don't cancel")
            }
          }
        )
      }
    }

    BackHandler(backHandlingEnabled) {
      // Confirm back press
      if (!changesMade) finish() // Safe to finish if no changes made
      else showConfirm = true
    }
    val cameraLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        imageTaken = true
      }

    fun submitClaimViaIntent() {
      val intent = Intent()

      if (claimDate.equals("")) {
        Toast.makeText(this, "You have not set the date", Toast.LENGTH_SHORT)
          .show()
        return
      }
      if (futureDate(claimDate)) {
        // Test is late.  Better solution would suppress future dates in picker .. but standard picker does not (yet) do this
        // TODO see https://github.com/wdullaer/MaterialDateTimePicker#additional-options
        Toast.makeText(this, "Don't you be claiming in the future, now", Toast.LENGTH_SHORT)
          .show()
        return
      }
      // Don't enforce amount is non-zero (or even positive) as we could have 'record, but no claim' and 'correction' entries

      Log.d("Submitted date", claimDate)
      Log.d("Submitted reason", claimReason)
      Log.d("Submitted paid", claimPaid.toString())
      Log.d("Submitted amount", claimAmount)
      try {
        claimAmount.toFloat()
      } catch (e: java.lang.NumberFormatException) {
        Toast.makeText(this, "$claimAmount isn't a valid amount of money", Toast.LENGTH_SHORT)
          .show()
        return
      }
      intent.putExtra("date", claimDate)
      intent.putExtra("reason", claimReason)
      intent.putExtra("paid", claimPaid)
      intent.putExtra("amount", claimAmount.toFloat())
      intent.putExtra("receiptUri", claimUri.toString())
      setResult(RESULT_OK, intent)
      finish()
    }

    @Composable
    fun GetReceiptImageButton(onClick: () -> Unit) {
      val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) {
        if (it) {
          changesMade = true
          cameraLauncher.launch(claimUri)
        } else {
          Toast.makeText(context, "No photo because you didn't grant permission", Toast.LENGTH_LONG)
            .show()
        }
      }

      Button(onClick = {
        val gotPermission =
          ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (gotPermission == PackageManager.PERMISSION_GRANTED) {
          changesMade = true
          cameraLauncher.launch(claimUri)
        } else {
          permissionRequest.launch(Manifest.permission.CAMERA)
        }
      }) {
        Icon(
          imageVector = Icons.Outlined.Add,
          contentDescription = "Add receipt"
        )
        Text("Add receipt")
      }
    }

    @Composable
    fun SubmitClaim(onClick: () -> Unit) {
      Button(
        onClick = {
          submitClaimViaIntent()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
      ) {
        Icon(
//        imageVector = Icons.Filled.AddCircle,//Icons.Default.Add,
          imageVector = Icons.Filled.ShoppingCart, // Whimsical
          tint = Color.Unspecified,
          contentDescription = "Submit claim"
        )
        Text(" Submit claim", color = Color.Black)
      }
    }

    @Composable
    fun CancelClaim(onClick: () -> Unit) {
      Button(
        onClick = {
          onBackPressed()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
      ) {
        Icon(
          imageVector = Icons.Filled.Close,
          tint = Color.Unspecified,
          contentDescription = "Cancel claim"
        )
        Text(" Cancel claim", color = Color.Black)
      }
    }

    @Composable
    fun AmountTextField() {
      var text = claimAmount
      ProvideTextStyle(TextStyle(color = if (validAmount) Color.Black else Color.Red)) { // TODO - cope with dark mode, although seems OK
        TextField(
          value = text,
          onValueChange = {
            text = it.replace("\n", "")
            changesMade = true
            try {
              claimAmount = text.replace(",", "") // Keep comma in visual
              claimAmount.toFloat()
              validAmount = true
            } catch (e: NumberFormatException) {
              validAmount = false
            }
          },
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
          label = { Text("Amount claimed") }
        )
      }
    }

    @Composable
    fun ReasonOutlinedTextField() {
      OutlinedTextField(
        value = claimReason,
        onValueChange = {
          claimReason = it.replace("\n", "")  // We don't want newlines
          changesMade = true
        },
        label = { Text("Reason for claim") }
      )
    }

    @Composable
    fun DateField(modifier: Modifier = Modifier) {

      var isValid by remember { mutableStateOf(validDate) }

      val calendar = Calendar.getInstance()

      val year = calendar.get(Calendar.YEAR)
      val month = calendar.get(Calendar.MONTH)
      val day = calendar.get(Calendar.DAY_OF_MONTH)

      val myDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, myYear: Int, myMonth: Int, myDayOfMonth: Int ->
          claimDate = "$myDayOfMonth/${myMonth + 1}/$myYear"
          changesMade = true
          validDate = !futureDate(claimDate)
          isValid = validDate
        }, year, month, day
      )

      Box()
      {
        Button(
          onClick = {
            myDatePickerDialog.show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = if (isValid) Color.Green else Color.Red)
        ) {
          if (claimDate.length == 0)
            Text("Enter date")
          else {
            Text("Date : $claimDate")
          }
        }
      }
    }

    @Composable
    fun VerticalReasonAndAmount() {
      Column {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier =
          Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center,
        ) {
          ReasonOutlinedTextField()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier =
          Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center,
        ) {
          AmountTextField()
        }
      }
    }

    @Composable
    fun HorizontalReasonAndAmount() {
      Box(
        modifier =
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ReasonOutlinedTextField()
          Spacer(modifier = Modifier.width(8.dp))
          AmountTextField()
        }
      }
    }

    @Composable
    fun VerticalDateAndReceipt() {  // Unused
      Column {
        DateField()
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          contentAlignment = Alignment.Center,
        ) {
          Column {
            GetReceiptImageButton {}
          }
        }
      }
    }

    @Composable
    fun HorizontalDateAndReceipt() {
      Row(verticalAlignment = Alignment.CenterVertically) {
        DateField()
        Spacer(modifier = Modifier.width(8.dp))
        GetReceiptImageButton {}
      }
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text("Create new claim")
          },
          navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
              Icon(Icons.Filled.ArrowBack, "backIcon")
            }
          },
          colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
          ),
        )
      },
      bottomBar = {
        BottomAppBar(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.primary,
        ) {
          Text(
            modifier = Modifier
              .fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            text = "Remember to check expenses rules these can be long, complicated and written in legalese", // Deliberately doesn't show fully in typical small print style!
          )
        }
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .padding(innerPadding),//.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Column(
          modifier = Modifier
            .verticalScroll(rememberScrollState())
        ) {
          Box(
            modifier =
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {

            when (currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass) {
              WindowWidthSizeClass.Expanded -> HorizontalReasonAndAmount() // orientation is landscape in most devices including foldables (width 840dp+)
              WindowWidthSizeClass.Medium -> HorizontalReasonAndAmount() // Most tablets are in landscape, larger unfolded inner displays in portrait (width 600dp+)
              WindowWidthSizeClass.Compact -> VerticalReasonAndAmount() // Most phones in portrait
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier =
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {

            when (currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass) {  // Always horizontal works
              WindowWidthSizeClass.Expanded -> HorizontalDateAndReceipt() // orientation is landscape in most devices including foldables (width 840dp+)
              WindowWidthSizeClass.Medium -> HorizontalDateAndReceipt() // Most tablets are in landscape, larger unfolded inner displays in portrait (width 600dp+)
              WindowWidthSizeClass.Compact -> HorizontalDateAndReceipt() // Most phones in portrait
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier =
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {
            Row {
              Text("Claim has already been paid", modifier = Modifier.align(CenterVertically))
              Checkbox(
                checked = claimPaid,
                modifier = Modifier.align(CenterVertically),
                onCheckedChange = { claimPaid = it; changesMade = true }
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier =
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
          ) {
            Row {
              CancelClaim {}
              Spacer(modifier = Modifier.width(8.dp))
              SubmitClaim {}
            }
          }
        }
      }
    }

    ConfirmCancel(
      show = showConfirm,
      onDismissRequest = { showConfirm = false },
      onConfirmation = {
        showConfirm = false
        // Delete URI ... as we don't want an orphan receipt image after cancel TODO - does not work.
        // Other data will just be lost as was never on filesystem.
        if (claimUri != null) {
          val result = this.getContentResolver().delete(claimUri, null, null)
          if (result == 0) {
            Log.e("Deletion of $claimUri.toString()", " UNSUCCESSFUL")
          } else {
            Log.i("Deletion of $claimUri.toString()", " successful")
          }
        }
        finish()
      },
      dialogTitle = "Confirm dialog",
      dialogText = "Confirmation of cancel.",
      icon = Icons.Filled.Warning
    )
  }
}