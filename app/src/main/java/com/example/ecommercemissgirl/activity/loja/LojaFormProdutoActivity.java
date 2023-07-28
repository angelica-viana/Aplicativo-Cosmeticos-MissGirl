package com.example.ecommercemissgirl.activity.loja;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommercemissgirl.R;
import com.example.ecommercemissgirl.adapter.CategoriaDialogAdapter;
import com.example.ecommercemissgirl.databinding.ActivityLojaFormProdutoBinding;
import com.example.ecommercemissgirl.databinding.BottomSheetFormProdutoBinding;
import com.example.ecommercemissgirl.databinding.DialogFormProdutoCategoriaBinding;
import com.example.ecommercemissgirl.helper.FirebaseHelper;
import com.example.ecommercemissgirl.model.Categoria;
import com.example.ecommercemissgirl.model.ImagemUpload;
import com.example.ecommercemissgirl.model.Produto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LojaFormProdutoActivity extends AppCompatActivity implements CategoriaDialogAdapter.OnClick {

    private DialogFormProdutoCategoriaBinding categoriaBinding;

    private final List<String> idsCategoriasSelecionadas = new ArrayList<>();
    private final List<String> categoriaSelecionadaList = new ArrayList<>();

    private final List<Categoria> categoriaList = new ArrayList<>();

    private final List<ImagemUpload> imagemUploadList = new ArrayList<>();
    private Produto produto;
    private boolean novoProduto = true;

    private ActivityLojaFormProdutoBinding binding;

    private int resultCode = 0;

    private String currentPhotoPath;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLojaFormProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getExtra();

        configClicks();

        iniciaComponentes();

        recuperaCategorias();

    }

    private void getExtra(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            produto = (Produto) bundle.getSerializable("produtoSelecionado");
            configProduto();
        }
    }

    private void configProduto(){
        novoProduto = false;

        idsCategoriasSelecionadas.addAll(produto.getIdsCategorias());

        binding.imageFake0.setVisibility(View.GONE);
        binding.imageFake1.setVisibility(View.GONE);
        binding.imageFake2.setVisibility(View.GONE);

        Glide.with(this)
                .load(produto.getUrlsImagens().get(0).getCaminhoImagem())
                .into(binding.imagemProduto0);

        Glide.with(this)
                .load(produto.getUrlsImagens().get(1).getCaminhoImagem())
                .into(binding.imagemProduto1);

        Glide.with(this)
                .load(produto.getUrlsImagens().get(2).getCaminhoImagem())
                .into(binding.imagemProduto2);

        binding.edtTitulo.setText(produto.getTitulo());
        binding.edtDescricao.setText(produto.getDescricao());
        binding.edtValorAntigo.setText(String.valueOf(produto.getValorAntigo() * 10));
        binding.edtValorAtual.setText(String.valueOf(produto.getValorAtual() * 10));
    }

    private void configClicks() {
        binding.include.include.ibVoltar.setOnClickListener(v -> finish());
        binding.imagemProduto0.setOnClickListener(v -> showBottomSheet(0));
        binding.imagemProduto1.setOnClickListener(v -> showBottomSheet(1));
        binding.imagemProduto2.setOnClickListener(v -> showBottomSheet(2));
    }

    private void configRv(){
        categoriaBinding.rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        categoriaBinding.rvCategorias.setHasFixedSize(true);
        CategoriaDialogAdapter categoriaDialogAdapter = new CategoriaDialogAdapter(idsCategoriasSelecionadas, categoriaList, this, this);
        categoriaBinding.rvCategorias.setAdapter(categoriaDialogAdapter);
    }

    public void showDialogCategorias(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustemAlertDialog2);

         categoriaBinding = DialogFormProdutoCategoriaBinding
                .inflate(LayoutInflater.from(this));

        categoriaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        categoriaBinding.btnSalvar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        if(categoriaList.isEmpty()){
            categoriaBinding.textInfo.setText("Nenhuma categoria cadastrada.");
        }else{
            categoriaBinding.textInfo.setText("");
        }
        categoriaBinding.progressBar.setVisibility(View.GONE);

        configRv();

        builder.setView(categoriaBinding.getRoot());

        dialog = builder.create();
        dialog.show();
    }

    private void recuperaCategorias(){
        DatabaseReference categoriaRef = FirebaseHelper.getDatabaseReference()
                .child("categorias");
        categoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    categoriaList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Categoria categoria = ds.getValue(Categoria.class);
                        categoriaList.add(categoria);
                    }
                    configuraCategoriasEdicao();
                }
                Collections.reverse(categoriaList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configuraCategoriasEdicao(){
        if(!novoProduto) { // Edição
            for (Categoria categoria : categoriaList) {
                if (produto.getIdsCategorias().contains(categoria.getId())) {
                    categoriaSelecionadaList.add(categoria.getNome());
                }
            }

            Collections.reverse(categoriaSelecionadaList);
            CategoriasSelecionadas();
        }
    }

    private void CategoriasSelecionadas(){
        StringBuilder categorias = new StringBuilder();
        for (int i = 0; i < categoriaSelecionadaList.size(); i++) {
            if(i != categoriaSelecionadaList.size() - 1){
                categorias.append(categoriaSelecionadaList.get(i)).append(", ");
            }else{
                categorias.append(categoriaSelecionadaList.get(i));
            }
        }

        if(!categoriaSelecionadaList.isEmpty()){
            binding.btnCategorias.setText(categorias);
        }else {
            binding.btnCategorias.setText("Nenhuma categoria selecionada.");
        }
    }

    public void validaDados(View view){
        String titulo = binding.edtTitulo.getText().toString().trim();
        String descricao = binding.edtDescricao.getText().toString().trim();
        double valorAntigo = (double) binding.edtValorAntigo.getRawValue() / 100;
        double valorAtual = (double) binding.edtValorAtual.getRawValue() / 100;

        if(!titulo.isEmpty()){
            if(!descricao.isEmpty()){
                if(valorAtual > 0){
                    if (!idsCategoriasSelecionadas.isEmpty()){
                        if(produto == null) produto = new Produto();

                        produto.setTitulo(titulo);
                        produto.setDescricao(descricao);
                        produto.setValorAtual(valorAtual);
                        if(valorAntigo > 0) produto.setValorAntigo(valorAntigo);
                        produto.setIdsCategorias(idsCategoriasSelecionadas);

                        if(novoProduto){ // Novo Produto
                            if(imagemUploadList.size() == 3){
                                for (int i = 0; i < imagemUploadList.size(); i++) {
                                    salvarImagemFirebase(imagemUploadList.get(i), i);
                                }
                            }else{
                                ocultaTeclado();
                                Toast.makeText(this, "Escolha 03 imagens para o produto.", Toast.LENGTH_SHORT).show();
                            }
                        }else { // Edição do produto

                            ocultaTeclado();

                            if(imagemUploadList.size() > 0){
                                for (int i = 0; i < imagemUploadList.size(); i++) {
                                    salvarImagemFirebase(imagemUploadList.get(i), i);
                                }
                            }else{
                                produto.salvar(false);
                            }
                        }
                    }else{
                        ocultaTeclado();
                        Toast.makeText(this, "Selecione pelo menos uma categoria para o produto.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    binding.edtValorAtual.setError("Informe um valor válido.");
                }
            }else{
                binding.edtDescricao.setError("Informação obrigatória.");
            }

        }else{
          binding.edtTitulo.setError("Informação obrigatória.");
        }

    }

    private void showBottomSheet(int code) {
        resultCode = code;

        BottomSheetFormProdutoBinding sheetBinding =
                BottomSheetFormProdutoBinding.inflate(LayoutInflater.from(this));

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                this, R.style.BottomSheetDialog);
        bottomSheetDialog.setContentView(sheetBinding.getRoot());
        bottomSheetDialog.show();

        sheetBinding.btnCamera.setOnClickListener(v -> {
            verificaPermissaoCamera();
            bottomSheetDialog.dismiss();
        });

        sheetBinding.btnGaleria.setOnClickListener(v -> {
            verificaPermissaoGaleria();
            bottomSheetDialog.dismiss();
        });

        sheetBinding.btnCancelar.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
    }

    private void verificaPermissaoCamera() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirCamera();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getBaseContext(), "Permissão Negada", Toast.LENGTH_SHORT).show();
            }
        };

        showDialogPermissao(
                permissionlistener,
                new String[]{Manifest.permission.CAMERA},
                "Se você não aceitar a permissão não poderá acessar a câmera do dispositivo, deseja ativar a permissão agora?"
        );
    }

    private void verificaPermissaoGaleria() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirGaleria();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getBaseContext(), "Permissão Negada", Toast.LENGTH_SHORT).show();
            }
        };

        showDialogPermissao(
                permissionlistener,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                "Se você não aceitar a permissão não poderá acessar a Galeria do dispositivo, deseja ativar a permissão agora?"
        );
    }

    private void showDialogPermissao(PermissionListener permissionListener, String[] permissoes, String msg) {
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedTitle("Permissão negada")
                .setDeniedMessage(msg)
                .setDeniedCloseButtonText("Não")
                .setGotoSettingButtonText("Sim")
                .setPermissions(permissoes)
                .check();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void abrirCamera() {
        switch (resultCode) {
            case 0:
                resultCode = 3;
                break;
            case 1:
                resultCode = 4;
                break;
            case 2:
                resultCode = 5;
                break;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.br.ecommerce.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            resultLauncher.launch(takePictureIntent);
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
    }

    private void configUpload(String caminhoImagem) {
        int request = 0;
        switch (resultCode) {
            case 0:
            case 3:
                request = 0;
                break;
            case 1:
            case 4:
                request = 1;
                break;
            case 2:
            case 5:
                request = 2;
                break;
        }

        ImagemUpload imagemUpload = new ImagemUpload(request, caminhoImagem);

        if (imagemUploadList.isEmpty()) {

            boolean encontrou = false;
            for (int i = 0; i < imagemUploadList.size(); i++) {
                if (imagemUploadList.get(i).getIndex() == request) {
                    encontrou = true;
                }
            }

            if (encontrou) {
                imagemUploadList.set(request, imagemUpload);
            } else {
                imagemUploadList.add(imagemUpload);
            }

        } else {
            imagemUploadList.add(imagemUpload);
        }
    }

    private void salvarImagemFirebase(ImagemUpload imagemUpload, int count) {
        int index = imagemUpload.getIndex();
        String caminhoImagem = imagemUpload.getCaminhoImagem();

        StorageReference storegeReference = FirebaseHelper.getStorageReference()
                .child("imagens")
                .child("produtos")
                .child(produto.getId())
                .child("imagem" + index + ".jpeg");

        UploadTask uploadTask = storegeReference.putFile(Uri.parse(caminhoImagem));
        uploadTask.addOnSuccessListener(taskSnapshot -> storegeReference.getDownloadUrl().addOnCompleteListener(task -> {

            imagemUpload.setCaminhoImagem(task.getResult().toString());

            if(novoProduto){
                produto.getUrlsImagens().add(imagemUpload);
            }else{
                produto.getUrlsImagens().set(index, imagemUpload);
            }

            if(imagemUploadList.size() == count + 1){
                produto.salvar(novoProduto);
                imagemUploadList.clear();

                if(novoProduto){
                    finish();
                }

            }

        })).addOnFailureListener(e -> Toast.makeText(
                this, "Ocorreu um erro com o upload, tente novamente.",
                Toast.LENGTH_SHORT).show());


    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {

                    String caminhoImagem;

                    if (resultCode <= 2) { // Galeria

                        Uri imagemSelecionada = result.getData().getData();


                        try {

                            caminhoImagem = imagemSelecionada.toString();

                            switch (resultCode) {
                                case 0:
                                    binding.imageFake0.setVisibility(View.GONE);
                                    binding.imagemProduto0.setImageBitmap(getBitmap(imagemSelecionada));
                                    break;
                                case 1:
                                    binding.imageFake1.setVisibility(View.GONE);
                                    binding.imagemProduto1.setImageBitmap(getBitmap(imagemSelecionada));
                                    break;
                                case 2:
                                    binding.imageFake2.setVisibility(View.GONE);
                                    binding.imagemProduto2.setImageBitmap(getBitmap(imagemSelecionada));
                                    break;
                            }

                            configUpload(caminhoImagem);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else { //Câmera

                        File file = new File(currentPhotoPath);
                        caminhoImagem = String.valueOf(file.toURI());

                        switch (resultCode) {
                            case 3:
                                binding.imageFake0.setVisibility(View.GONE);
                                binding.imagemProduto0.setImageURI(Uri.fromFile(file));
                                break;
                            case 4:
                                binding.imageFake1.setVisibility(View.GONE);
                                binding.imagemProduto1.setImageURI(Uri.fromFile(file));
                                break;
                            case 5:
                                binding.imageFake2.setVisibility(View.GONE);
                                binding.imagemProduto2.setImageURI(Uri.fromFile(file));
                                break;
                        }

                        configUpload(caminhoImagem);

                    }


                }

            }
    );

    private Bitmap getBitmap(Uri caminhoUri) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), caminhoUri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), caminhoUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void iniciaComponentes(){
        binding.edtValorAntigo.setLocale(new Locale("PT", "br"));
        binding.edtValorAtual.setLocale(new Locale("PT", "br"));

        if(novoProduto){
            binding.include.textTitulo.setText("Novo produto");
        }else{
            binding.include.textTitulo.setText("Edição produto");
        }
    }

    //Oculta o teclado do dispositivo
    private void ocultaTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.edtTitulo.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onClickListener(Categoria categoria) {
        if(!idsCategoriasSelecionadas.contains(categoria.getId())){ //add
            idsCategoriasSelecionadas.add(categoria.getId());
            categoriaSelecionadaList.add(categoria.getNome());
        }else{ //Del
            idsCategoriasSelecionadas.remove(categoria.getId());
            categoriaSelecionadaList.remove(categoria.getNome());
        }

        CategoriasSelecionadas();
    }
}