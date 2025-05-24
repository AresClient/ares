#version 330 core

in vec2 texCoord;
in vec4 vertColor;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, texCoord) * vertColor;
    if(fragColor.a == 0.0) discard;
}
