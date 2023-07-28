package com.example.ecommercemissgirl.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.ecommercemissgirl.databinding.ActivityRecuperaContaBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;

public class RecuperaContaActivity extends AppCompatActivity {

    private ActivityRecuperaContaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperaContaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configClicks();
    }

    public void validaDados(View view){
        String email = binding.editEmail.getText().toString().trim();

        if(!email.isEmpty()){
            binding.progressbar.setVisibility(View.VISIBLE);
            recuperaConta(email);
        }else{
           binding.editEmail.requestFocus();
           binding.editEmail.setError("Informe seu email.");
        }
    }

    private void recuperaConta(String email){
        FirebaseHelper.getAuth().sendPasswordResetEmail(
                email
        ).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               Toast.makeText(this, "Acabamos de enviar um link para o e-mail informado.", Toast.LENGTH_SHORT).show();
           }else{
               Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
            binding.progressbar.setVisibility(View.GONE);
        });

    }

    private void configClicks(){
        binding.include.ibVoltar.setOnClickListener(view -> finish());
    }
}