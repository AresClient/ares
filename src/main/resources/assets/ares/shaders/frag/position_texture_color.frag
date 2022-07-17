#version 330 core

in vec2 texCoord;
in vec4 vertColor;

uniform sampler2D theTexture;

out vec4 fragColor;

void main() {
    fragColor = texture(theTexture, texCoord) * vertColor;
    if(fragColor.a == 0.0) discard;
}
