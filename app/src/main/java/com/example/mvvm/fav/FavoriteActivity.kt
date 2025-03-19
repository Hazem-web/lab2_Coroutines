package com.example.mvvm.fav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mvvm.db.LocalDataStoreImp
import com.example.mvvm.models.Product
import com.example.mvvm.network.RemoteDataSourceImp
import com.example.mvvm.online.OnlineProductViewModel
import com.example.mvvm.online.OnlineProductViewModelFactory
import com.example.mvvm.repo.ProductsRepositoryImp
import com.example.mvvm.ui.theme.MVVMTheme
import kotlinx.coroutines.launch

class FavoriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MVVMTheme {
                val factory= FavProductViewModelFactory(
                    ProductsRepositoryImp(
                    localDataStore = LocalDataStoreImp.getInstance(this@FavoriteActivity),
                    remoteDataSource = RemoteDataSourceImp.getInstance()
                )
                )
                val viewModel= ViewModelProvider(this, factory = factory).get(FavProductViewModel::class.java)
                FavUI(viewModel)
            }
        }
    }
}

@Composable
fun FavUI(viewModel: FavProductViewModel) {
    viewModel.getAll()
    val dataState = viewModel.product.observeAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            items(dataState.value?.size ?: 0) {
                val current = dataState.value?.get(it)
                ProductItem(product = current) {
                    viewModel.deleteProduct(current)
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "item:$current deleted",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductItem(product: Product?, action: ()->Unit){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        GlideImage(
            model = product?.imgUrl,
            contentDescription = product?.description,
            modifier = Modifier
                .height(100.dp)
                .width(120.dp)
        )
        Column(
            modifier = Modifier.width(150.dp)
        ) {
            Text(
                text = product?.title?:"",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = product?.category?:"",
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (product!=null) product.price.toString() + "$" else "",
                color = Color(0.0f, 0.0f, 0.0f, 0.302f),
                textAlign = TextAlign.Center
            )
        }
        Button(onClick = action) {
            Text(text = "remove from Favorites", textAlign = TextAlign.Center)
        }
    }
}