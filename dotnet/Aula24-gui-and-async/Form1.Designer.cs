namespace Aula24_gui_and_async {

    partial class Form1 {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.pictureBox2 = new System.Windows.Forms.PictureBox();
            this.url1 = new System.Windows.Forms.TextBox();
            this.url2 = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.status = new System.Windows.Forms.TextBox();
            this.url3 = new System.Windows.Forms.TextBox();
            this.pictureBox3 = new System.Windows.Forms.PictureBox();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).BeginInit();
            this.SuspendLayout();
            // 
            // pictureBox1
            // 
            this.pictureBox1.Location = new System.Drawing.Point(12, 183);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(201, 187);
            this.pictureBox1.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox1.TabIndex = 0;
            this.pictureBox1.TabStop = false;
            // 
            // pictureBox2
            // 
            this.pictureBox2.Location = new System.Drawing.Point(227, 183);
            this.pictureBox2.Name = "pictureBox2";
            this.pictureBox2.Size = new System.Drawing.Size(211, 187);
            this.pictureBox2.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox2.TabIndex = 1;
            this.pictureBox2.TabStop = false;
            // 
            // url1
            // 
            this.url1.Location = new System.Drawing.Point(49, 28);
            this.url1.Name = "url1";
            this.url1.Size = new System.Drawing.Size(622, 22);
            this.url1.TabIndex = 2;
            this.url1.Text = "http://www.sic.pt/favicon.ico";
            // 
            // url2
            // 
            this.url2.Location = new System.Drawing.Point(49, 56);
            this.url2.Name = "url2";
            this.url2.Size = new System.Drawing.Size(622, 22);
            this.url2.TabIndex = 3;
            this.url2.Text = "http://www.rtp.pt/favicon.ico";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(49, 137);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(99, 30);
            this.button1.TabIndex = 4;
            this.button1.Text = "Show";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button4_Click);
            // 
            // status
            // 
            this.status.Location = new System.Drawing.Point(12, 414);
            this.status.Name = "status";
            this.status.Size = new System.Drawing.Size(659, 22);
            this.status.TabIndex = 6;
            // 
            // url3
            // 
            this.url3.Location = new System.Drawing.Point(49, 84);
            this.url3.Name = "url3";
            this.url3.Size = new System.Drawing.Size(622, 22);
            this.url3.TabIndex = 7;
            this.url3.Text = "http://www.tvi.pt/favicon.ico";
            // 
            // pictureBox3
            // 
            this.pictureBox3.Location = new System.Drawing.Point(453, 183);
            this.pictureBox3.Name = "pictureBox3";
            this.pictureBox3.Size = new System.Drawing.Size(218, 187);
            this.pictureBox3.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox3.TabIndex = 8;
            this.pictureBox3.TabStop = false;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(732, 448);
            this.Controls.Add(this.pictureBox3);
            this.Controls.Add(this.url3);
            this.Controls.Add(this.status);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.url2);
            this.Controls.Add(this.url1);
            this.Controls.Add(this.pictureBox2);
            this.Controls.Add(this.pictureBox1);
            this.Name = "Form1";
            this.Text = "Form1";
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox pictureBox1;
        private System.Windows.Forms.PictureBox pictureBox2;
        private System.Windows.Forms.TextBox url1;
        private System.Windows.Forms.TextBox url2;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.TextBox status;
        private System.Windows.Forms.TextBox url3;
        private System.Windows.Forms.PictureBox pictureBox3;
    }
}

