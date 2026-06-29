package hrd.h4rdykrft.gui;
import hrd.h4rdykrft.gui.Image;
import hrd.h4rdykrft.render.Shader;
import hrd.h4rdykrft.block.Blocks;
public class InventorySlot extends UIElement {
    public int itemId;
    private Image imageslot;
    private Image image;
    public InventorySlot(int x, int y, int itemId){
        super(x,y,50,50);
        this.itemId = itemId;
        this.imageslot = new Image(x,y,50,50,"textures/slot.png");
        System.out.println(itemId);
        this.image = new Image(x+2,y+2,46,46,"textures/inventory/"+Blocks.get((byte) this.itemId).invId+".png");
    };
    @Override
    public void render(Shader shader) {
        this.image.x = this.x + 2;
        this.image.y = this.y + 2;
        this.imageslot.x = this.x;
        this.imageslot.y = this.y;

        this.imageslot.render(shader);
        if (this.itemId != 0){
            this.image.render(shader);
        }
    }
    public void setItemId(int itemId){
        this.itemId = itemId;
        this.image = new Image(x+2,y+2,46,46,"textures/inventory/"+Blocks.get((byte) this.itemId).invId+".png");
    }
}