#version 330 core

in vec2 texCoord;

uniform sampler2D theTexture;

out vec4 fragColor;

void main() {
    fragColor = texture(theTexture, texCoord);
    if(fragColor.a == 0.0) discard;
}
