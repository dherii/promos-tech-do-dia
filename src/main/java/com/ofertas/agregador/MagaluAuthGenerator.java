package com.ofertas.agregador;

import com.microsoft.playwright.*;
import java.nio.file.Paths;

public class MagaluAuthGenerator {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            System.out.println("=====================================================");
            System.out.println("Navegador aberto! Faça o login na sua conta Magalu.");
            System.out.println("Quando você estiver vendo o painel /admin,");
            System.out.println("volte a este terminal e APERTE ENTER para salvar a sessão.");
            System.out.println("=====================================================");
            
            page.navigate("https://www.magazinevoce.com.br/admin");
            
            // Pausa o código até você dar ENTER no terminal
            System.in.read();
            
            context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("magalu_session.json")));
            System.out.println("Sessão salva com sucesso no arquivo magalu_session.json!");
            
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}