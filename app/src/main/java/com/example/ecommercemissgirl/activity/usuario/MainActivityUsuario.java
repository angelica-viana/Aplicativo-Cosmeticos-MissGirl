package com.example.ecommercemissgirl.activity.usuario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.widget.Toast;


import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.databinding.ActivityMainUsuarioBinding;


public class MainActivityUsuario extends AppCompatActivity {

    private ActivityMainUsuarioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

       int id = getIntent().getIntExtra("id", 0);
       if(id != 0) direcionaAcesso(id);

    }

    private void direcionaAcesso(int id){
        switch (id){
            case 1:
                binding.bottomNavigationView.setSelectedItemId(R.id.menu_pedido);
                break;
            case 2:
                binding.bottomNavigationView.setSelectedItemId(R.id.menu_carrinho);
                break;
            default:
                Toast.makeText(this, "Acesso inválido, verifique por favor.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}