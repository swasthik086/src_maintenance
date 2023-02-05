package com.suzuki.pojo;

public class MapListCustomClass {
        private int id;
        private String name;

        public MapListCustomClass() {
        }

        public MapListCustomClass(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }