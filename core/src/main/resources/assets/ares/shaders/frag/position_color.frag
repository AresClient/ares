#version 330 core

in vec4 vertColor;

out vec4 fragColor;

void main() {
    fragColor = vertColor;
    if(fragColor.a == 0.0) discard;
}
