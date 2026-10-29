package com.example.postreader.Model;

public class User {
    private int id;
    private String name;
    private String username;
    private String email;
    private String website;
    private Company company;

    public static class Company {
        private String name;
        private String catchPhrase;
        private String bs;

        public Company() {
        }

        public Company(String name, String catchPhrase, String bs) {
            this.name = name;
            this.catchPhrase = catchPhrase;
            this.bs = bs;
        }

        public String getName() {
            return name != null ? name : "";
        }

        public String getCatchPhrase() {
            return catchPhrase != null ? catchPhrase : "";
        }

        public String getBs() {
            return bs != null ? bs : "";
        }
    }

    public User() {
    }

    public User(int id, String name, String username, String email, String website, Company company) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.website = website;
        this.company = company;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "Anonymous";
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getWebsite() {
        return website != null ? website : "";
    }

    public Company getCompany() {
        return company;
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}

