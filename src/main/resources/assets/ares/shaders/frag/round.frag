#version 330 core

in vec2 vertUV;
in vec4 vertColor;

uniform float radius = 0.6; // 0 to 1

out vec4 fragColor;

void main() {
    float distance = (length(max(vertUV*vertUV - radius, 0)) + radius) * 0.5;
    float alpha = smoothstep(0.0, 1.5, (0.5 - distance) / fwidth(distance));
    fragColor = vec4(vertColor.rgb, vertColor.a * alpha);
}
