package cz.tvoje.quiettools;

public class SearchEntry {

    public String name;       // lowercase jméno pokémona
    public int r, g, b;       // barva ESP
    public boolean enabled;   // zapnuto/vypnuto

    public SearchEntry(String name, int r, int g, int b) {
        this.name = name.toLowerCase().trim();
        this.r = r;
        this.g = g;
        this.b = b;
        this.enabled = true;
    }
}