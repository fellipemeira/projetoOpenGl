import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
//import org.lwjgl.opengl.GLContext;
//import org.lwjgl.system.MemoryUtil;

public class Tutorial07 {

    int SCREEN_WIDTH = 640;
    int SCREEN_HEIGHT = 480;

    private static int textureID;
    private static float mTextureWidth;
    private static float mTextureHeight;
    private static PNGDecoder textureDecoder;
    private static ByteBuffer textureData;
    private LFRect[] gArrowClips = new LFRect[4];

    public Tutorial07() {

        if (!glfwInit()) {
            System.err.println("Falha ao inicializar GLFW!");
            System.exit(1);
        }

        long win = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Janela", 0, 0);

        glfwShowWindow(win);
        glfwMakeContextCurrent(win);
        GL.createCapabilities();
        glEnable(GL_TEXTURE_2D);

        //Define a viewport
        glViewport( 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT );

        //Inicializa a Matriz de projeção
        glMatrixMode( GL_PROJECTION );
        glLoadIdentity();
        glOrtho( 0.0, SCREEN_WIDTH, SCREEN_HEIGHT, 0.0, 1.0, -1.0 );

        //Inicializa a Matriz Modelview
        glMatrixMode( GL_MODELVIEW );
        glLoadIdentity();

        //PNGDecoder, ferramenta de auxilio para obtenção de informações de uma imagem PNG, como largura, altura e formato de cor.
        try (InputStream inputStream = new FileInputStream("res/arrows.png")) {
            textureDecoder = new PNGDecoder(inputStream);
            textureData = BufferUtils.createByteBuffer(4 * textureDecoder.getWidth() * textureDecoder.getHeight());
            textureDecoder.decode(textureData, textureDecoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            textureData.flip();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (!glfwWindowShouldClose(win)) {

            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT);

            glBindTexture(GL_TEXTURE_2D, textureID);


            mTextureWidth = textureDecoder.getWidth();
            mTextureHeight = textureDecoder.getHeight();

            //Mapeia uma porção de uma imagem de textura especificada em cada primitiva gráfica para a qual a texturização está ativa.
            GL11.glTexImage2D(GL_TEXTURE_2D, //Tipo da textura (1D, 2D, 3D)
                    0, //Nível, Sempre ajuste isso para zero
                    GL_RGBA, //Formato interno, o RGBA funciona melhor
                    textureDecoder.getWidth(), // Largura da textura em pixels
                    textureDecoder.getHeight(), // Altura da textura em pixels
                    0, //Border, always set this to zero
                    GL_RGBA, //Formato de textura, no nosso caso, isso é RGBA (você pode encontrar dinamicamente o tipo de textura com PNGDecoder)
                    GL_UNSIGNED_BYTE, //Tipo de dados de textura, este é sempre byte não assinado (isso deve tocar um sino com programadores C / C ++)
                    textureData);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);


            //Define o recorte dos retângulos
            gArrowClips[0] = new LFRect();
            gArrowClips[0].x = 0.f;
            gArrowClips[0].y = 0.f;
            gArrowClips[0].w = 128.f;
            gArrowClips[0].h = 128.f;

            gArrowClips[1] = new LFRect();
            gArrowClips[1].x = 128.f;
            gArrowClips[1].y = 0.f;
            gArrowClips[1].w = 128.f;
            gArrowClips[1].h = 128.f;

            gArrowClips[2] = new LFRect();
            gArrowClips[2].x = 0.f;
            gArrowClips[2].y = 128.f;
            gArrowClips[2].w = 128.f;
            gArrowClips[2].h = 128.f;

            gArrowClips[3] = new LFRect();
            gArrowClips[3].x = 128.f;
            gArrowClips[3].y = 128.f;
            gArrowClips[3].w = 128.f;
            gArrowClips[3].h = 128.f;

            //Renderiza as setas            
            render(0.f, 0.f, gArrowClips[0]);
            render(SCREEN_WIDTH - gArrowClips[1].w, 0.f, gArrowClips[1]);
            render(0.f, SCREEN_HEIGHT - gArrowClips[2].h, gArrowClips[2]);
            render(SCREEN_WIDTH - gArrowClips[3].w, SCREEN_HEIGHT - gArrowClips[3].h, gArrowClips[3]);

            glfwSwapBuffers(win);

        }
        glfwTerminate();
    }

    private void render(float x, float y, LFRect clip) {
    	//Remova quaisquer transformações anteriores
        glLoadIdentity();

        //Coordenadas de textura
        float texTop = 0.f;
        float texBottom = 1.f;
        float texLeft = 0.f;
        float texRight = 1.f;

        //Coordenadas do vertex
        float quadWidth = mTextureWidth/2;
        float quadHeight = mTextureHeight/2;

        //Manipula o recorte
        if (clip != null) {
            //Coordenadas de textura
            texLeft = clip.x / mTextureWidth;
            texRight = (clip.x + clip.w) / mTextureWidth;
            texTop = clip.y / mTextureHeight;
            texBottom = (clip.y + clip.h) / mTextureHeight;

            //Coordenadas do vertex
            quadWidth = clip.w;
            quadHeight = clip.h;
        }
        //Move para o ponto de renderização
        glTranslatef( x, y, 0.f );
        
        //Define o ID da textura
        glBindTexture(GL_TEXTURE_2D, textureID);

        //Renderiza o quadrado com textura
        glBegin(GL_QUADS);        
        glTexCoord2f(  texLeft,    texTop ); glVertex2f(       0.f,        0.f );
        glTexCoord2f( texRight,    texTop ); glVertex2f( quadWidth,        0.f );
        glTexCoord2f( texRight, texBottom ); glVertex2f( quadWidth, quadHeight );
        glTexCoord2f(  texLeft, texBottom ); glVertex2f(       0.f, quadHeight );
        glEnd();
    }

  
    private class LFRect {
        float x;
        float y;
        float w;
        float h;
    }

    public static void main(String[] args) {
        new Tutorial07();
    }

}
