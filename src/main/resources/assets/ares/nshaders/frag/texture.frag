#version 330 core

in vec4 vertColor;
in vec2 vertUV;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, vertUV) * vertColor;
    if(fragColor.a == 0.0) discard;
}
