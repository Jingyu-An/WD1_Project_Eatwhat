package com.example.eatwhat.controller;

import com.example.eatwhat.dao.UserRepository;
import com.example.eatwhat.model.Recipe;
import com.example.eatwhat.model.User;
import com.example.eatwhat.service.RecipeService;
import com.example.eatwhat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Controller
public class EatwhatController {

    private String loginErrorSite;

    private RequestCache requestCache = new HttpSessionRequestCache();
    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/home")
    public String recipeBoard(@RequestParam String site, Model model) {
        model.addAttribute("site", site);
        return "redirect:/" + site;
    }

    @GetMapping("/login")
    public String login(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "site", required = false) String site,
            @RequestParam(value = "error", defaultValue = "false") Boolean error,
            HttpServletRequest request,
            Model model
    ) {
        if (user != null && user.isEnabled()) {
            if (user.getAuthorities().contains("ADMIN")) {
                return "redirect:/manager";
            } else if (user.getAuthorities().contains("USER")) {
                return "redirect:/user";
            }
        }
        if (site == null) {
            SavedRequest savedRequest = requestCache.getRequest(request, null);
            if (savedRequest != null) {
                site = estimateSite(savedRequest.getRedirectUrl());
            }
        }
        model.addAttribute("error", error);
        model.addAttribute("site", site);

        return "loginForm";
    }

    private String estimateSite(String referer) {
        if(referer == null)
            return "study.html";
        try {
            URL url = new URL(referer);
            String path = url.getPath();
            if(path != null){
                if(path.startsWith("/user")) return "user";
                if(path.startsWith("/recipe")) return "recipe";
                if(path.startsWith("/manager")) return "manager";
            }
            String query = url.getQuery();
            if(query != null){
                if(path.startsWith("/site=user")) return "user";
                if(path.startsWith("/site=recipe")) return "recipe";
                if(path.startsWith("/site=manager")) return "manager";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "recipe.html";
    }

//    @RequestMapping(value = "/login", method = RequestMethod.GET)
//    public String loginPage(HttpServletRequest request, @RequestParam String site, Model model) {
//        String referrer = request.getHeader("Referer");
//        request.getSession().setAttribute("prevPage", referrer);
//        model.addAttribute("site", site);
//        loginErrorSite = site;
//        return "loginForm";
//    }


    @PostMapping("/login")
    public String loginPost(@RequestParam String site, Model model) {
        System.out.println("login page : " + site);
        model.addAttribute("site", site);
        return "redirect:/" + site;
    }


    @GetMapping("/login-error")
    public String loginError(Model model) {
        System.out.println("Login ERROR site : " + loginErrorSite);
        model.addAttribute("loginError", true);
        model.addAttribute("site", loginErrorSite);
        return "loginForm";
    }

//    @GetMapping("/signup")
//    public String signUp(@RequestParam String site) {
//        System.out.println("signup");
//        return "redirect:/" + site + "/signup";
//    }
    @GetMapping("/signup")
    public String signUp(
            @RequestParam String site,
            HttpServletRequest request
    ){
        if(site == null) {
            site = estimateSite(request.getParameter("referer"));
        }
        return "redirect:/"+site+"/signup";
    }

    @GetMapping("/access-denied")
    public String accessDenied(){
        return "/accessDenied";
    }
}