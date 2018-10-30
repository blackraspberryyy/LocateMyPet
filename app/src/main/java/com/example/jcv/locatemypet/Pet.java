package com.example.jcv.locatemypet;

/**
 * Created by asus-pc on 6/24/2017.
 */

public class Pet {
    public int pet_id;
    public String pet_code;
    public String pet_type;
    public String pet_name;
    public String pet_char;
    public String pet_owner_name;
    public String pet_owner_contact;

    public int getPet_id() {
        return pet_id;
    }

    public void setPet_id(int pet_id) {
        this.pet_id = pet_id;
    }

    public String getPet_code() {
        return pet_code;
    }

    public void setPet_code(String pet_code) {
        this.pet_code = pet_code;
    }

    public String getPet_type() {
        return pet_type;
    }

    public void setPet_type(String pet_type) {
        this.pet_type = pet_type;
    }

    public String getPet_name() {
        return pet_name;
    }

    public void setPet_name(String pet_name) {
        this.pet_name = pet_name;
    }

    public String getPet_char() {
        return pet_char;
    }

    public void setPet_char(String pet_char) {
        this.pet_char = pet_char;
    }

    public String getPet_owner_name() {
        return pet_owner_name;
    }

    public void setPet_owner_name(String pet_owner_name) {
        this.pet_owner_name = pet_owner_name;
    }

    public String getPet_owner_contact() {
        return pet_owner_contact;
    }

    public void setPet_owner_contact(String pet_owner_contact) {
        this.pet_owner_contact = pet_owner_contact;
    }

    public Pet(int pet_id, String pet_code, String pet_type, String pet_name, String pet_char, String pet_owner_name, String pet_owner_contact) {

        this.pet_id = pet_id;
        this.pet_code = pet_code;
        this.pet_type = pet_type;
        this.pet_name = pet_name;
        this.pet_char = pet_char;
        this.pet_owner_name = pet_owner_name;
        this.pet_owner_contact = pet_owner_contact;
    }
}
