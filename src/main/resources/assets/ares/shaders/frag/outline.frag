#version 330 core

in vec2 texCoord;

uniform vec2 viewportSize;
uniform float lineWeight = 1.0f;

uniform sampler2D theTexture;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(theTexture, texCoord);
    float dx = (1.0 / viewportSize.x) * lineWeight;
    float dy = (1.0 / viewportSize.y) * lineWeight;
    float d = min(dx, dy);

    float delta = 0.0;
    vec4 color = texColor;
    for(int x = -1; x < 2; ++x) {
        for(int y = -1; y < 2; ++y) {
            if(x == 0 && y == 0) continue;
            vec4 colorT = texture(theTexture, vec2(texCoord.x + d * x, texCoord.y + d * y));
            float deltaT = abs(texColor.a - colorT.a);
            if(deltaT > delta) {
                delta = deltaT;
                color = colorT;
            }
        }
    }

    fragColor = vec4(color.rgb, delta);
    if(fragColor.a == 0.0) discard;
}
