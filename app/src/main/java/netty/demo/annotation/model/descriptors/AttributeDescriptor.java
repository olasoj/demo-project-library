package netty.demo.annotation.model.descriptors;

public record AttributeDescriptor(String name, String type, String accessModifier) {

    @Override
    public String toString() {
        return String.join(" ", this.accessModifier, this.type, this.name);
    }
}
